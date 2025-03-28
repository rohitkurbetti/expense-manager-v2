package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.dtos.Day;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayListAdapter extends BaseAdapter {

    private Context context;
    private List<Day> nestedItemList;
    private int qty;

    @Override
    public int getCount() {
        return nestedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return nestedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public DayListAdapter(Context context, List<Day> nestedItemList) {
        this.context = context;
        this.nestedItemList = nestedItemList;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_firebase1, parent, false);
        }
        TextView nestedItemTitle = convertView.findViewById(R.id.nestedItemTitle);
        TextView nestedItemExpense = convertView.findViewById(R.id.nestedItemExpense);
        CheckBox checkboxDayDelete = convertView.findViewById(R.id.checkboxDayDelete);
        ImageView btnDeleteDateEntry = convertView.findViewById(R.id.btnDeleteDateEntry);

        Day nestedItem = nestedItemList.get(position);
        nestedItemTitle.setText(nestedItemList.get(position).getDayName());
        nestedItemExpense.setText("\u20B9"+nestedItemList.get(position).getDayTotal());

        // Handle button click event
        convertView.setOnClickListener(v -> {

            populateNestedItemsPopup(nestedItem.getDtoJsonEntityList(), nestedItem.getDayName(), nestedItem.getDayTotal());


        });

        checkboxDayDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    btnDeleteDateEntry.setVisibility(View.VISIBLE);
                } else {
                    btnDeleteDateEntry.setVisibility(View.GONE);
                }
            }
        });


        btnDeleteDateEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM-yyyy");
                String dayName = nestedItem.getDayName();
                LocalDate localDate = LocalDate.parse(dayName);
                String monthYearFmtted = LocalDate.parse(dayName).format(monthYearFormatter);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("invoices");
                databaseReference.child("/"+localDate.getYear()+"/"+monthYearFmtted+"/"+dayName).removeValue()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();

                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete data: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        });
            }
        });


        return convertView;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void populateNestedItemsPopup(List<DtoJsonEntity> expenseItemList, String subItemName, Long subItemExpenseTotalAmount) {

        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.nested_items_popup, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        ListView popupListView = dialogView.findViewById(R.id.popupListView);

        Map<String,Integer> otherAmountMap = new HashMap<>();
        List<DtoJsonEntity> expenseItemListTemp = new ArrayList<>();
        expenseItemListTemp.clear();
        expenseItemListTemp.addAll(expenseItemList);
        Map<String, Integer> mapResovled = parseJsonAndCalculate(expenseItemListTemp, otherAmountMap);
        expenseItemListTemp.clear();

        mapResovled.forEach( (k, v) -> {
            expenseItemListTemp.add(new DtoJsonEntity(k,
                    Long.valueOf(InvoiceConstants.ITEM_PRICE_MAP.containsKey(k.toUpperCase()) ?
                            v * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(k.toUpperCase(),0) :
                            otherAmountMap.getOrDefault(k,0)),
                    Long.valueOf(v)));
        });

        NestedItemAdapter adapter = new NestedItemAdapter(context, expenseItemListTemp);
        popupListView.setAdapter(adapter);


        TextView tvDate = dialogView.findViewById(R.id.tvItemDatePopup);
        TextView tvItemTotalPopup = dialogView.findViewById(R.id.tvItemTotalPopup);
        Button btnClose = dialogView.findViewById(R.id.btnClosePopup);
//        btnDeletePopup = dialogView.findViewById(R.id.btnDeletePopup);


        if(subItemName != null && !subItemName.isEmpty() && !subItemName.equals("")) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(subItemName, dateTimeFormatter);

            String frmtteddate = DateTimeFormatter.ofPattern("dd-MMM-yy").format(date);
            tvDate.setText(String.valueOf(frmtteddate));
        } else {
            tvDate.setText("");
        }

        tvItemTotalPopup.setText(String.valueOf("\u20B9"+subItemExpenseTotalAmount));
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

//        btnDeletePopup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                expenseItemList.forEach(i -> {
//                    if(i.getChecked()) {
//                        deleteFromFirebaseCloud(i);
//                    }
//                });
//                dialog.dismiss();
//                // Navigate back to MainActivity after successful deletion
//                Intent intent = new Intent(context, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Clear the activity stack
//                context.startActivity(intent);
//            }
//        });


    }

    private Map<String, Integer> parseJsonAndCalculate(List<DtoJsonEntity> expenseItemList, Map<String, Integer> otherAmountMap) {

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

                if(otherItems != null && otherItems.size() > 0) {
                    items.addAll(otherItems);
                }
                System.err.println("items :: "+ items);
                for (CustomItem i : items) {
                    if (itemSaleMap.containsKey(i.getName())) {
                        int tempQty = itemSaleMap.get(i.getName());
                        qty = (int) i.getSliderValue() + tempQty;
                        amount = otherAmountMap.get(i.getName()) + i.getAmount();
                        itemSaleMap.put(i.getName(), qty);
                        item.setName(i.getName());
                    } else {
                        amount = 0;
                        itemSaleMap.put(i.getName(), (int) i.getSliderValue());
                        qty = (int) i.getSliderValue();
                        amount = amount + i.getAmount();
//                        System.err.println("Item: "+i.getName()+" "+qty+" Amt"+amount);
                        item.setName(i.getName());

                    }
                    otherAmountMap.put(i.getName(), amount);
                }
                item.setQty(Long.valueOf(qty));
                item.setTotal(Long.valueOf(amount));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.err.println("map: "+ itemSaleMap + " amount >> "+ amount);
        System.err.println("Othermap: "+ otherAmountMap);
        return itemSaleMap;
    }


}
