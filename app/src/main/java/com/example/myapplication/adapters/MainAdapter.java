package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.CloudSummary;
import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.dtos.Month;
import com.example.myapplication.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context context;
    private List<Month> mainItemList;
    private Map<String, Integer> amountMap= new HashMap<>();

    public MainAdapter(Context context, List<Month> mainItemList) {
        this.context = context;
        this.mainItemList = mainItemList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        Month mainItem = mainItemList.get(position);

        holder.mainItemTitle.setText(mainItem.getMonthName());
        holder.mainItemTotalExpense.setText("\u20B9"+mainItem.getMonthTotal());

        // If the item is expanded, show the ListView and adjust its height
        if (mainItem.isExpanded()) {
            holder.nestedListView.setVisibility(View.VISIBLE);

            // Set the ListView adapter
            DayListAdapter nestedListAdapter = new DayListAdapter(holder.itemView.getContext(), mainItem.getDayList());
            holder.nestedListView.setAdapter(nestedListAdapter);

            // Dynamically adjust the ListView height based on its content
            Utils.setListViewHeightBasedOnChildren(holder.nestedListView);

            holder.chevronIcon.setImageResource(R.drawable.chevron_up_svgrepo_com__1_); // Show up chevron
        } else {
            holder.nestedListView.setVisibility(View.GONE);
            holder.chevronIcon.setImageResource(R.drawable.chevron_down_svgrepo_com); // Show down chevron
        }



        // Toggle expansion on click
        holder.itemView.setOnClickListener(v -> {
            mainItem.setExpanded(!mainItem.isExpanded());
            notifyItemChanged(position);  // Refresh this item
        });

        holder.printMonthlyReportBtn.setOnClickListener(new View.OnClickListener() {
            private Integer amountGrandtotal=0;

            @Override
            public void onClick(View v) {
                List<DtoJsonEntity> mList = new ArrayList<>();

//                mainItemList.forEach(month -> {
                        mainItem.getDayList().forEach(d -> {
                        mList.addAll(d.getDtoJsonEntityList());
                    });
//                });
                Map<String, Integer> mapResovled = parseJsonAndCalculate(mList);

                mapResovled.forEach((k, val) -> {
                    amountGrandtotal += (InvoiceConstants.ITEM_PRICE_MAP.containsKey(k.toUpperCase(Locale.ENGLISH)) ?
                            val * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(k.toUpperCase(Locale.ENGLISH),0) :
                            amountMap.getOrDefault(k.toUpperCase(Locale.ENGLISH), 0));
                    System.err.println(k+" "+amountGrandtotal);
                });

                File pdfFile = generateMonthlyReport(context, mapResovled, amountMap, mainItem.getMonthName(), amountGrandtotal);
                if (pdfFile != null) {
                    showSnackbarWithOpenAction(pdfFile, v);
                }
                amountGrandtotal=0;
            }
        });

        holder.deleteMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monthName = mainItem.getMonthName();
                String yearName = monthName.substring( monthName.length() - (monthName.length() - 4 ));
                confirmDeletDialog(yearName, monthName);
            }

            private void confirmDeletDialog(String yearName, String monthName) {

                LayoutInflater inflater = LayoutInflater.from(context);
                View customView = inflater.inflate(R.layout.confirm_delete_alertdialog, null);
                // Find the ImageView in the custom layout
                ImageView gifImageView = customView.findViewById(R.id.dialogGifIcon);
                TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
                dialogMessage.setText("Do you want to delete records from "+yearName+"/"+monthName);

                // Load the GIF into the ImageView using Glide
                Glide.with(context)
                        .asGif()
                        .load(R.raw.confirm) // Replace with your actual GIF file path
                        .into(gifImageView);


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(customView);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMonthdata(yearName, monthName);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void deleteMonthdata(String yearName, String monthName) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        String deviceModel = sharedPreferences.getString("model", Build.MODEL);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel+"/"+"invoices");
        databaseReference.child("/"+yearName+"/"+monthName).removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }

    private Map<String, Integer> parseJsonAndCalculate(List<DtoJsonEntity> expenseItemList) {

        int qty=0;
        int amount=0;
        Map<String, Integer> itemSaleMap = new HashMap<>();
        for (DtoJsonEntity item : expenseItemList) {
            String itemJson = item.getItemListJsonStr();
            ObjectMapper objectMapper = new ObjectMapper();

            try {

                DtoJson itemObject = objectMapper.readValue(itemJson, DtoJson.class);

                List<CustomItem> items = itemObject.getItemList();
                List<CustomItem> otherItems = itemObject.getOtherItemsList();
                amount = 0;

                if(otherItems !=null && otherItems.size()>0) {
                    items.addAll(otherItems);
                }

                for (CustomItem i : items) {
                    if (itemSaleMap.containsKey(i.getName().toUpperCase(Locale.ENGLISH))) {
                        int tempQty = itemSaleMap.get(i.getName().toUpperCase(Locale.ENGLISH));
                        qty = (int) i.getSliderValue() + tempQty;
                        amount = amount + i.getAmount();
                        itemSaleMap.put(i.getName().toUpperCase(Locale.ENGLISH), qty);
                        item.setName(i.getName());
                    } else {
                        amount = 0;
                        itemSaleMap.put(i.getName().toUpperCase(Locale.ENGLISH), (int) i.getSliderValue());
                        qty = (int) i.getSliderValue();
                        amount = amount + i.getAmount();
                        item.setName(i.getName());

                    }
                    amountMap.put(i.getName().toUpperCase(Locale.ENGLISH), amount);
                }
                item.setQty(Long.valueOf(qty));
                item.setTotal(Long.valueOf(amount));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.err.println("map: "+ itemSaleMap);
        System.err.println("amountMap: "+ amountMap);
        return itemSaleMap;
    }

    private File generateMonthlyReport(Context context, Map<String, Integer> mainItemList, Map<String, Integer> amountMap, String monthName, int amountGrandtotal) {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint totalPaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(18f);
        canvas.drawText("Monthly Sales Report ("+monthName+")", pageInfo.getPageWidth() / 2, 40, titlePaint);

        // Total
        totalPaint.setTextAlign(Paint.Align.LEFT);
        totalPaint.setColor(Color.BLACK);
        totalPaint.setTextSize(13f);
        canvas.drawText("Total \u20B9"+amountGrandtotal, 15, 60, totalPaint);


        // Table headers
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);
        canvas.drawText("Product", 30, 100, paint);
        canvas.drawText("Quantity", 140, 100, paint);
        canvas.drawText("Amount", 220, 100, paint);

        // Draw horizontal line
        canvas.drawLine(10, 110, pageInfo.getPageWidth() - 10, 110, paint);

        // Table rows
        final int[] y = {130};

        // Table headers
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);
        mainItemList.forEach((k,v) -> {
            canvas.drawText(k+"", 30, y[0], paint);
            canvas.drawText(String.valueOf(v), 140, y[0], paint);
            canvas.drawText(String.valueOf(InvoiceConstants.ITEM_PRICE_MAP.containsKey(k) ?
                            v * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(k,0) : amountMap.getOrDefault(k,0)
                    ), 220, y[0], paint);
            y[0] += 20;
        });

        pdfDocument.finishPage(page);

        File folder = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOCUMENTS + "/Gajanan Coldrink House/Reports");

        if(!folder.exists()) {
            folder.mkdirs();
        }

        // Save PDF to external storage
        File pdfFile = new File(folder, "Monthly_Sales_Report_"+monthName+".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error while generating PDF!", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
        return pdfFile;
    }

    private void showSnackbarWithOpenAction(File pdfFile, View view) {
        Snackbar snackbar = Snackbar.make(view, "PDF generated successfully!", Snackbar.LENGTH_LONG);
        snackbar.setAction("Open", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdf(pdfFile);
            }
        });
        snackbar.show();
    }

    private void openPdf(File pdfFile) {
        // Create intent
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            Toast.makeText(context, "No application available to open PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mainItemList.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView mainItemTitle,mainItemTotalExpense;
        ListView nestedListView;
        ImageView chevronIcon,deleteMonthBtn,printMonthlyReportBtn;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            mainItemTitle = itemView.findViewById(R.id.mainItemTitle);
            nestedListView = itemView.findViewById(R.id.nestedListView);
            mainItemTotalExpense = itemView.findViewById(R.id.mainItemTotalExpense);
            chevronIcon = itemView.findViewById(R.id.chevronIcon);
            deleteMonthBtn = itemView.findViewById(R.id.deleteMonthBtn);
            printMonthlyReportBtn = itemView.findViewById(R.id.printMonthlyReportBtn);
        }
    }
}

