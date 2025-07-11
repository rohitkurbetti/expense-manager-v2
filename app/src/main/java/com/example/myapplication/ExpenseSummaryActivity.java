package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;

import com.example.myapplication.adapters.Expense;
import com.example.myapplication.adapters.MyExpandableListAdapter;
import com.example.myapplication.dtos.DayDto;
import com.example.myapplication.dtos.MonthDto;
import com.example.myapplication.dtos.YearDto;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseSummaryActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private MyExpandableListAdapter adapter;

    private List<String> listGroupTitles = new ArrayList<>();
    private Map<String, List<String>> listChildData = new HashMap<>();
    private DatabaseReference rootRef;
    List<YearDto> yearList;
    private AppCompatSpinner yearExpenseSpinner;
    ProgressDialog progressDialog;
    ProgressBar loadingSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_summary);

        expandableListView = findViewById(R.id.expandableListView);
        yearExpenseSpinner = findViewById(R.id.yearExpenseSpinner);
//        progressDialog = new ProgressDialog(this);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        loadSpinner(yearExpenseSpinner);

        yearExpenseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString();
                fetchExpenses(selectedYear);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        String deviceModel = sharedPreferences.getString("model", Build.MODEL);

        rootRef = FirebaseDatabase.getInstance().getReference(deviceModel+"/"+"expenses");

        fetchExpenses(null);

    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default": setTheme(R.style.Base_Theme_MyApplication); break;
            case "Red": setTheme(R.style.AppTheme_Red); break;
            case "Blue": setTheme(R.style.AppTheme_Blue); break;
            case "Green": setTheme(R.style.AppTheme_Green); break;
            case "Purple": setTheme(R.style.AppTheme_Purple); break;
            case "Orange": setTheme(R.style.AppTheme_Orange); break;
            case "Teal": setTheme(R.style.AppTheme_Teal); break;
            case "Pink": setTheme(R.style.AppTheme_Pink); break;
            case "Cyan": setTheme(R.style.AppTheme_Cyan); break;
            case "Lime": setTheme(R.style.AppTheme_Lime); break;
            case "Brown": setTheme(R.style.AppTheme_Brown); break;
            case "Mint": setTheme(R.style.AppTheme_Mint); break;
            case "Coral": setTheme(R.style.AppTheme_Coral); break;
            case "Steel": setTheme(R.style.AppTheme_Steel); break;
            case "Lavender": setTheme(R.style.AppTheme_Lavender); break;
            case "Mustard": setTheme(R.style.AppTheme_Mustard); break;
            default: setTheme(R.style.Base_Theme_MyApplication); break;
        }
    }

    private void loadSpinner(AppCompatSpinner yearExpenseSpinner) {
        // Get the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Generate a list of years (e.g., from 1900 to current year)
        List<String> years = new ArrayList<>();
        for (int year = 2021; year <= currentYear+3; year++) {
            years.add(String.valueOf(year));
        }

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );

        // Set the layout for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the Spinner
        yearExpenseSpinner.setAdapter(adapter);

        // Optionally, set the current year as the selected item
        yearExpenseSpinner.setSelection(adapter.getPosition(String.valueOf(currentYear)));
    }

    private void fetchExpenses(String selectedYear) {
//        progressDialog.setMessage("Fetching from cloud database");
//        progressDialog.show();
        showLoading(true);

        Map<String, Long> expExpAmount = new HashMap<>();
        Map<String, Long> expMonthAmount = new HashMap<>();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot yearSnapshot) {
//                progressDialog.dismiss();
                showLoading(false);

                listGroupTitles.clear();
                listChildData.clear();

                yearList = new ArrayList<>();

                for (DataSnapshot yearNode : yearSnapshot.getChildren()) {

                    YearDto yearDto = new YearDto();
                    yearDto.setYear(yearNode.getKey());

                    if(yearDto.getYear().equalsIgnoreCase(selectedYear)) {
                        List<MonthDto> monthDtoList = new ArrayList<>();

                        for (DataSnapshot monthNode : yearNode.getChildren()) {

                            String expMonth = monthNode.getKey(); // May-2025

                            MonthDto monthDto = new MonthDto();
                            monthDto.setMonth(expMonth);

                            List<String> expenseList = new ArrayList<>();
                            Long expMonthAmountVal = 0L;

                            List<DayDto> dayDtoList = new ArrayList<>();

                            for (DataSnapshot dateNode : monthNode.getChildren()) {
                                String expenseDate = dateNode.getKey();

                                DayDto dayDto = new DayDto();

                                dayDto.setDay(expenseDate);

    //                            for (DataSnapshot expenseSnap : dateNode.getChildren()) {
                                    Expense expense = dateNode.getValue(Expense.class);
                                    if (expense != null) {
                                        expExpAmount.put(expenseDate, expense.getExpenseAmount().longValue());
                                        expenseList.add(expenseDate);
                                        expMonthAmountVal += expense.getExpenseAmount();

                                        dayDto.setExpense(expense);

                                    }
    //                            }
                                dayDtoList.add(dayDto);
                            }
                            expMonthAmount.put(expMonth, expMonthAmountVal);
                            listGroupTitles.add(expMonth);
                            listChildData.put(expMonth, expenseList);

                            monthDto.setDayDtoList(dayDtoList);
                            monthDtoList.add(monthDto);
                        }

                        yearDto.setMonthList(monthDtoList);
                        yearList.add(yearDto);
                    }
                }

                yearList.stream().forEach(e -> System.out.println(e.getMonthList()));

                adapter = new MyExpandableListAdapter(ExpenseSummaryActivity.this, listGroupTitles, listChildData, expExpAmount, expMonthAmount);
                expandableListView.setAdapter(adapter);
                expandableListView.setLayoutAnimation(
                        AnimationUtils.loadLayoutAnimation(ExpenseSummaryActivity.this, R.anim.layout_fade_in)
                );
                expandableListView.setOnGroupExpandListener(groupPosition -> {
                    expandableListView.smoothScrollToPosition(groupPosition);
                });
                expandableListView.scheduleLayoutAnimation();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                progressDialog.dismiss();
                showLoading(false);
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        loadingSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        expandableListView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}