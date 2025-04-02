package com.example.myapplication.fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ExpenseRecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapters.Expense;
import com.example.myapplication.adapters.ExpenseAdapter;
import com.example.myapplication.adapters.InvoiceAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.database.ExpenseDbHelper;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExpensesFragment extends Fragment {

    private static final String INR_SYMBOL = "₹";
    private ExpenseDbHelper db;
    private ProgressDialog progressDialog;
    private List<Expense> expenseList, filteredExpenseList;
    private ListView listViewFragExp;
    private EditText edtFilterDateFragExp;
    private TextView totalAmountTextViewFragExp;
    private TextView totalRecordsTextViewFragExp;
    private ExpenseAdapter adapter;
    private ImageView noSqliteDataImageViewFragExp;
    private TextView noSqliteDataTextViewFragExp;
    private ImageView deleteBtnFragExp;
    private LinearLayout selectionOverlayFragExp;
    public static TextView itemSelectedTxtFragExp;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_expenses, container, false);

        db = new ExpenseDbHelper(view.getContext());
        progressDialog = new ProgressDialog(getContext());
        listViewFragExp = view.findViewById(R.id.listViewFragExp);
        edtFilterDateFragExp = view.findViewById(R.id.edtFilterDateFragExp);
        Button resetFilterBtnFragExp = view.findViewById(R.id.resetFilterBtnFragExp);
        totalRecordsTextViewFragExp = view.findViewById(R.id.totalRecordsTextViewFragExp);
        totalAmountTextViewFragExp = view.findViewById(R.id.totalAmountTextViewFragExp);
        noSqliteDataImageViewFragExp = view.findViewById(R.id.noSqliteDataImageViewFragExp);
        noSqliteDataTextViewFragExp = view.findViewById(R.id.noSqliteDataTextViewFragExp);
        deleteBtnFragExp = view.findViewById(R.id.deleteBtnFragExp);
        selectionOverlayFragExp = view.findViewById(R.id.selectionOverlayFragExp);
        itemSelectedTxtFragExp = view.findViewById(R.id.itemSelectedTxtFragExp);


        expenseList = new ArrayList<>();
        filteredExpenseList = new ArrayList<>();

        getAllExpensesFromInDb();

        edtFilterDateFragExp.setOnClickListener(v -> showDateTimeDialog());
        resetFilterBtnFragExp.setOnClickListener(v -> resetFilters());



        filteredExpenseList.clear();
        filteredExpenseList.addAll(expenseList);

        //set data in adapter

        adapter = new ExpenseAdapter(view.getContext(), filteredExpenseList, selectionOverlayFragExp);
        listViewFragExp.setAdapter(adapter);

        deleteBtnFragExp.setOnClickListener(v -> {
            deleteSelectedExpensesById();
            selectionOverlayFragExp.setVisibility(View.GONE);
        });



        return view;
    }

    private void deleteSelectedExpensesById() {
        IntStream invIdStream = expenseList.stream().filter(Expense::getChecked).mapToInt(Expense::getId);
        Set<Integer> expIds = invIdStream.boxed().collect(Collectors.toSet());

        db.deleteExpensesByIds(expIds);

//        db.deleteFirestoreInvoicesbyIds(invoiceList,progressDialog);
        getAllExpensesFromInDb();
        resetAllExpensesCheckBoxes();
        if(!expenseList.isEmpty()) {
            filteredExpenseList.clear();
            filteredExpenseList.addAll(expenseList);
        }
        adapter.notifyDataSetChanged();
    }

    private void resetAllExpensesCheckBoxes() {
        expenseList.stream().map(expense ->
        {
            expense.setChecked(false);
            return expense;
        });
//        List<Invoice> checkedList = invoiceList.stream().collect(Collectors.toList());
//        Log.d(">>>", checkedList.toString());
        selectionOverlayFragExp.setVisibility(View.GONE);

    }

    private void resetFilters() {
        filterExpenses("");
        edtFilterDateFragExp.setText(null);
    }

    private void showDateTimeDialog() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();

        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.set(year, month, dayOfMonth);
                edtFilterDateFragExp.setText(android.text.format.DateFormat.format("yyyy-MM-dd", date));
                filterExpenses(String.valueOf(edtFilterDateFragExp.getText()));
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void filterExpenses(String date) {
        filteredExpenseList.clear();
        if (date.isEmpty()) {
            filteredExpenseList.addAll(expenseList);
        } else {
            for (Expense expense : expenseList) {
                if (expense.getExpenseDate().contains(date)) {
                    filteredExpenseList.add(expense);
                }
            }
        }
        adapter.updateList(filteredExpenseList);

        totalRecordsTextViewFragExp.setText("Total Records: "+filteredExpenseList.size());
        OptionalInt totalSumOptional = filteredExpenseList.stream().mapToInt(i-> (int) i.getExpenseAmount()).reduce((a, b) -> a+b);
        int totalSum = 0;
        if(totalSumOptional.isPresent()) {
            totalSum = totalSumOptional.getAsInt();
        }

        totalAmountTextViewFragExp.setText("Total: "+INR_SYMBOL+totalSum);

    }

    private void getAllExpensesFromInDb() {
        Cursor cursor = db.getAllExpenses();
        if(cursor.getCount()> 0) {
//            listView.setVisibility(View.VISIBLE);
//            noSqliteDataImageView.setVisibility(View.GONE);
//            noSqliteDataTextView.setVisibility(View.GONE);
            expenseList.clear();
            int qty=0;
            int amount=0;
            while(cursor.moveToNext()) {
                int expId = cursor.getInt(0);
                String expPart = cursor.getString(1);
                int expAmount = cursor.getInt(2);
                String expDateTime = cursor.getString(3);
                String expDate = cursor.getString(4);
                int yBalance = cursor.getInt(5);
                int sales = cursor.getInt(6);
                int balance = cursor.getInt(7);


                expenseList.add(new Expense(expId, expPart, expAmount, expDateTime, expDate, yBalance, sales, balance));
            }
            totalRecordsTextViewFragExp.setText("Total Records: "+cursor.getCount());
            OptionalInt totalSumOptional = expenseList.stream().mapToInt(i-> (int) i.getExpenseAmount()).reduce((a, b) -> a+b);
            int totalSum = 0;
            if(totalSumOptional.isPresent()) {
                totalSum = totalSumOptional.getAsInt();
            }
            totalAmountTextViewFragExp.setText("Total: "+INR_SYMBOL+totalSum);
        } else {
            listViewFragExp.setVisibility(View.GONE);
            noSqliteDataImageViewFragExp.setVisibility(View.VISIBLE);
            noSqliteDataTextViewFragExp.setVisibility(View.VISIBLE);
            totalRecordsTextViewFragExp.setText("Total Records: "+cursor.getCount());
            totalAmountTextViewFragExp.setText("Total: "+INR_SYMBOL+"0");
        }
    }
}
