package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.Invoice;
import com.example.myapplication.dtos.Item;
import com.example.myapplication.fragments.InvoicesFragment;
import com.example.myapplication.utils.PDFGeneratorUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvoiceAdapter extends BaseAdapter {
    private Context context;
    private List<Invoice> invoiceList;
    private LayoutInflater inflater;
    private final LinearLayout selectionOverlay;

    public InvoiceAdapter(Context context, List<Invoice> invoiceList, LinearLayout selectionOverlay) {
        this.context = context;
        this.invoiceList = invoiceList;
        this.inflater = LayoutInflater.from(context);
        this.selectionOverlay = selectionOverlay;
    }

    @Override
    public int getCount() {
        return invoiceList.size();
    }

    @Override
    public Object getItem(int position) {
        return invoiceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return invoiceList.get(position).getInvoiceId();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_invoice, parent, false);
        }

        TextView txtInvoiceId = convertView.findViewById(R.id.txtInvoiceId);
//        TextView txtItemList = convertView.findViewById(R.id.txtItemList);
        TextView txtTotal = convertView.findViewById(R.id.txtTotal);
        TextView txtCreatedDateTime = convertView.findViewById(R.id.txtCreatedDateTime);
        TextView txtCreatedDate = convertView.findViewById(R.id.txtCreatedDate);
        ImageView invoiceDetailsBtn = convertView.findViewById(R.id.invoiceDetailsBtn);
        CheckBox checkBoxInvoiceId = convertView.findViewById(R.id.checkBoxInvoiceId);
        ImageView invoiceGeneratorBtn = convertView.findViewById(R.id.invoiceGeneratorBtn);

        Invoice invoice = invoiceList.get(position);

        txtInvoiceId.setText("Invoice ID: " + invoice.getInvoiceId());
//        txtItemList.setText(invoice.getFormattedItemList());
        txtTotal.setText("Total: \u20B9" + invoice.getTotal());
        txtCreatedDateTime.setText("Created: " + invoice.getCreatedDateTime());
        txtCreatedDate.setText("Date: " + invoice.getCreatedDate());

        Map<String, Integer> itemSaleMap = invoice.getItemSaleMap();

        invoiceGeneratorBtn.setOnClickListener(v -> {



//            List<CustomItem> itemList = getParserJsonList(invoice.getItemListJson());
            Gson gson = new Gson();
            Type objectType = new TypeToken<DtoJson>() {}.getType();
            DtoJson dtoJson = gson.fromJson(invoice.getItemListJson(), objectType);
            File pdfFile = PDFGeneratorUtil.generateInvoice(dtoJson, invoice.getInvoiceId(), context);
            openGeneratedPDF(pdfFile);



        });

        invoiceDetailsBtn.setOnClickListener(v -> invoiceDetails(itemSaleMap));

        checkBoxInvoiceId.setChecked(invoice.getChecked());

        checkBoxInvoiceId.setOnCheckedChangeListener((v, isChecked) -> {
            if(isChecked) {
                invoice.setChecked(true);
            } else {
                invoice.setChecked(false);
            }

            boolean isCheckedAny = invoiceList.stream().anyMatch(Invoice::getChecked);

            if(isCheckedAny) {
                //show delete button
                selectionOverlay.setVisibility(View.VISIBLE);
            } else {
                //hide delete button
                selectionOverlay.setVisibility(View.GONE);
            }

            long checkedCInvoicesCount = invoiceList.stream().filter(Invoice::getChecked).count();
            InvoicesFragment.itemSelectedTxt.setText(checkedCInvoicesCount+" Items selected");
        });

        return convertView;
    }

    private void openGeneratedPDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Open PDF");
        try {
            context.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            // Handle the case where no PDF reader is installed
            Toast.makeText(context, "No application found to open PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private List<CustomItem> getParserJsonList(String itemListJson) {
        JsonObject jsonObject = (new JsonParser()).parse(itemListJson).getAsJsonObject();
        JsonArray listArr = jsonObject.getAsJsonArray("itemList");
        JsonArray listOtherArr = jsonObject.getAsJsonArray("otherItemsList");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CustomItem>>() {}.getType();
        List<CustomItem> itemList = gson.fromJson(listArr, listType);
        List<CustomItem> otherItemList = gson.fromJson(listOtherArr, listType);
        itemList.addAll(otherItemList);
        return itemList;
    }

    private void invoiceDetails(Map<String, Integer> itemSaleMap) {

        List<Item> items = new ArrayList<>();

        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            String itemName = entry.getKey();
            int itemQty = entry.getValue();

            int amount = InvoiceConstants.ITEM_PRICE_MAP.containsKey(itemName.toUpperCase()) ?
                    itemQty * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(itemName.toUpperCase(), 0) :
                    0;


            items.add(new Item(itemName, itemQty, amount));
        }


        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.nested_items_popup, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        LinearLayout subTitleLinearLayout = dialogView.findViewById(R.id.subTitleLinearLayout);
        ListView popupListView = dialogView.findViewById(R.id.popupListView);
        TextView popupTitle = dialogView.findViewById(R.id.popupTitle);
        TextView tvItemDatePopup = dialogView.findViewById(R.id.tvItemDatePopup);
        TextView tvItemTotalPopup = dialogView.findViewById(R.id.tvItemTotalPopup);

        popupTitle.setText("Invoice Details Breakdown");
        subTitleLinearLayout.setVisibility(View.GONE);
        tvItemDatePopup.setVisibility(View.GONE);
        tvItemTotalPopup.setVisibility(View.GONE);

        Button btnClose = dialogView.findViewById(R.id.btnClosePopup);


        InvoiceDetailViewAdapter adapter = new InvoiceDetailViewAdapter(context, items);
        popupListView.setAdapter(adapter);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();



        // Close button handler
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }

    public void updateList(List<Invoice> filteredList) {
        invoiceList = filteredList;
        notifyDataSetChanged();
    }
}

