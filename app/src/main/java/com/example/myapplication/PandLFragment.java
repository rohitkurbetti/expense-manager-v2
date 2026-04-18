package com.example.myapplication;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.NetResultAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.database.ExpenseDbHelper;
import com.example.myapplication.dtos.NetResult;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PandLFragment extends Fragment {

    DatabaseHelper databaseHelper;
    ExpenseDbHelper expenseDbHelper;
    TextView pAndLTV;

    String INR = "₹";

    private RecyclerView recyclerView;
    private NetResultAdapter adapter;
    private List<NetResult> invoiceList;

    private TextView tvTotalSales, tvTotalExpenses, tvNetProfit;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pand_l, container, false);
//        pAndLTV = view.findViewById(R.id.pAndLTV);
        databaseHelper = new DatabaseHelper(getContext());
        expenseDbHelper = new ExpenseDbHelper(getContext());


        initializeViews(view);
        setupRecyclerView();

        // Get today's date
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 12; i++) {
            today = today.minusMonths(i);


            // Get first day of current month
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);

            // Format dates as "2025-11-01", "2025-11-07"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String startDate = firstDayOfMonth.format(formatter);
            String endDate = "";
            if (i > 0) {
                LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
                endDate = lastDay.format(formatter);
            } else {
                endDate = today.format(formatter);
            }


            DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH);

            String monthYear = firstDayOfMonth.format(monthYearFormatter);


            Cursor cursor = databaseHelper.getPeriodRecords(startDate, endDate);
            Long total = 0L;
            Long expensesTotal = 0L;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    total += cursor.getLong(cursor.getColumnIndexOrThrow("total"));

                }
            }
            Log.i("total Sales>> ", "" + total);

            Cursor cursor1 = expenseDbHelper.getExpensesPeriod(startDate, endDate);
            if (cursor1.getCount() > 0) {
                while (cursor1.moveToNext()) {
                    expensesTotal += cursor1.getLong(cursor1.getColumnIndexOrThrow("expenseAmount"));
                }
            }
            Log.i("total expenses>> ", "" + expensesTotal);

            long profit = total - expensesTotal;

//        pAndLTV.setText(monthYear + "\n" + "Sales: " + total + "\n" + "Expenses: " + expensesTotal + "\n" + "P&L: " + profit);
//        invoiceList.add(new NetResult("November 2025", total, expensesTotal, profit, "2025-11-08"));
            loadInvoiceData(monthYear, total, expensesTotal, profit);
        }

        updateSummary();
        return view;
    }

    private void updateSummary() {
        double totalSales = 0, totalExpenses = 0, totalProfit = 0;

        for (NetResult invoice : invoiceList) {
            totalSales += invoice.getSales();
            totalExpenses += invoice.getExpenses();
            totalProfit += invoice.getProfitLoss();
        }

        tvTotalSales.setText(String.format("₹%.2f", totalSales));
        tvTotalExpenses.setText(String.format("₹%.2f", totalExpenses));
        tvNetProfit.setText(String.format("₹%.2f", totalProfit));

        // Set color for net profit
        if (totalProfit >= 0) {
            tvNetProfit.setTextColor(ContextCompat.getColor(getContext(), R.color.profit_green));
        } else {
            tvNetProfit.setTextColor(ContextCompat.getColor(getContext(), R.color.loss_red));
        }
    }

    private void loadInvoiceData(String monthYear, Long total, Long expensesTotal, long profit) {
        // Add sample data - you can replace this with your actual data
        if (total != 0)
            invoiceList.add(new NetResult(monthYear, total, expensesTotal, profit, "2025-11-08"));
        adapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        invoiceList = new ArrayList<>();
        adapter = new NetResultAdapter(invoiceList, getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Add item decoration for spacing
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewInvoices);
        tvTotalSales = view.findViewById(R.id.tvTotalSales);
        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        tvNetProfit = view.findViewById(R.id.tvNetProfit);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        // Get today's date
        LocalDate today = LocalDate.now();

        // Get first day of current month
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);

        // Format dates as "2025-11-01", "2025-11-07"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String startDate = firstDayOfMonth.format(formatter);
        String endDate = today.format(formatter);

        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);

        String monthYear = firstDayOfMonth.format(monthYearFormatter);


        Cursor cursor = databaseHelper.getPeriodRecords(startDate, endDate);
        Long total = 0L;
        Long expensesTotal = 0L;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                total += cursor.getLong(cursor.getColumnIndexOrThrow("total"));

            }
        }
        Log.i("total Sales>> ", "" + total);

        Cursor cursor1 = expenseDbHelper.getExpensesPeriod(startDate, endDate);
        if (cursor1.getCount() > 0) {
            while (cursor1.moveToNext()) {
                expensesTotal += cursor1.getLong(cursor1.getColumnIndexOrThrow("expenseAmount"));
            }
        }
        Log.i("total expenses>> ", "" + expensesTotal);

        long profit = total - expensesTotal;

//        pAndLTV.setText(monthYear + "\n" + "Sales: " + INR + total + "\n" + "Expenses: " + INR + expensesTotal + "\n" + "P&L: " + INR + profit);
    }
}