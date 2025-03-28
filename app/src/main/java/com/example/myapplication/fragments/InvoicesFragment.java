package com.example.myapplication.fragments;

import android.app.DatePickerDialog;
import android.app.MediaRouteButton;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.SqliteDataActivity;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapters.InvoiceAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class InvoicesFragment extends Fragment {

    private static final String INR_SYMBOL = "₹";
    private EditText edtFilterDate;
    private TextView totalAmountTextView;
    private TextView totalRecordsTextView;
    private ListView listView;
    private InvoiceAdapter adapter;
    private List<Invoice> invoiceList, filteredList;
    private DatabaseHelper db;
    private ImageView noSqliteDataImageView;
    private TextView noSqliteDataTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoices, container, false);

        db = new DatabaseHelper(view.getContext());
        listView = view.findViewById(R.id.listView);
        edtFilterDate = view.findViewById(R.id.edtFilterDate);
        Button resetFilterBtn = view.findViewById(R.id.resetFilterBtn);
        totalRecordsTextView = view.findViewById(R.id.totalRecordsTextView);
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        noSqliteDataImageView = view.findViewById(R.id.noSqliteDataImageView);
        noSqliteDataTextView = view.findViewById(R.id.noSqliteDataTextView);
        invoiceList = new ArrayList<>();
        filteredList = new ArrayList<>();

        try {
            getAllInvoicesFromInDb();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        edtFilterDate.setOnClickListener(v -> showDateTimeDialog());
        resetFilterBtn.setOnClickListener(v -> resetFilters());
        filteredList.addAll(invoiceList);
        adapter = new InvoiceAdapter(view.getContext(), filteredList);
        listView.setAdapter(adapter);


        return view;
    }

    private void resetFilters() {
        filterInvoices("");
        edtFilterDate.setText(null);
    }

    private void showDateTimeDialog() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();

        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.set(year, month, dayOfMonth);
                edtFilterDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd", date));
                filterInvoices(String.valueOf(edtFilterDate.getText()));
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void filterInvoices(String date) {
        filteredList.clear();
        if (date.isEmpty()) {
            filteredList.addAll(invoiceList);
        } else {
            for (Invoice invoice : invoiceList) {
                if (invoice.getCreatedDate().contains(date)) {
                    filteredList.add(invoice);
                }
            }
        }
        adapter.updateList(filteredList);

        totalRecordsTextView.setText("Total Records: "+filteredList.size());
        OptionalInt totalSumOptional = filteredList.stream().mapToInt(i-> (int) i.getTotal()).reduce((a, b) -> a+b);
        int totalSum = 0;
        if(totalSumOptional.isPresent()) {
            totalSum = totalSumOptional.getAsInt();
        }

        totalAmountTextView.setText("Total: "+INR_SYMBOL+totalSum);

    }

    private void getAllInvoicesFromInDb() throws IOException {
        Cursor cursor = db.getAllInvoices();
        if(cursor.getCount()> 0) {
            listView.setVisibility(View.VISIBLE);
            noSqliteDataImageView.setVisibility(View.GONE);
            noSqliteDataTextView.setVisibility(View.GONE);
            invoiceList.clear();
            int qty=0;
            int amount=0;
            while(cursor.moveToNext()) {
                int invId = cursor.getInt(0);
                String itemListJson = cursor.getString(1);
                long total = cursor.getLong(2);
                String createdDateTime = cursor.getString(3);
                String createdDate = cursor.getString(4);

                ObjectMapper objectMapper = new ObjectMapper();

                DtoJson itemObject = objectMapper.readValue(itemListJson, DtoJson.class);

                List<CustomItem> items = itemObject.getItemList();
                Map<String, Integer> itemSaleMap = new HashMap<>();

                for (CustomItem i : items) {
                    if (itemSaleMap.containsKey(i.getName())) {
                        int tempQty = itemSaleMap.get(i.getName());
                        qty = (int) i.getSliderValue() + tempQty;
                        amount = i.getAmount();
                        itemSaleMap.put(i.getName(), qty);
                    } else {
                        amount = 0;
                        itemSaleMap.put(i.getName(), (int) i.getSliderValue());
                        qty = (int) i.getSliderValue();
                        amount = amount + i.getAmount();

                    }
                }



                invoiceList.add(new Invoice(invId, itemListJson, total, createdDateTime, createdDate, itemSaleMap));
            }
            totalRecordsTextView.setText("Total Records: "+cursor.getCount());
            OptionalInt totalSumOptional = invoiceList.stream().mapToInt(i-> (int) i.getTotal()).reduce((a, b) -> a+b);
            int totalSum = 0;
            if(totalSumOptional.isPresent()) {
                totalSum = totalSumOptional.getAsInt();
            }

            totalAmountTextView.setText("Total: "+INR_SYMBOL+totalSum);
        } else {
            listView.setVisibility(View.GONE);
            noSqliteDataImageView.setVisibility(View.VISIBLE);
            noSqliteDataTextView.setVisibility(View.VISIBLE);
        }
    }
}

