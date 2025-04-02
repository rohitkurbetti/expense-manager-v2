package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.database.ExpenseDbHelper;
import com.example.myapplication.dtos.ExpenseParticularsDto;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseActivity extends AppCompatActivity {

    private EditText etExpenseParticulars, etExpenseAmount, etExpenseDateTime;
    private Button btnSaveExpense;
    private ExpenseDbHelper expenseDbHelper;
    private DatabaseHelper databaseHelper;
    private TextView textViewExpenseTotal,textViewBalanceTotal,textViewDate;
    private RecyclerView expenseRecyclerView;
    private ExpenseRecyclerViewAdapter expenseRecyclerViewAdapter;
    public static List<ExpenseRecyclerView> expenseItems = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button on Action Bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        // Initialize views
        etExpenseParticulars = findViewById(R.id.etExpenseParticulars);
        etExpenseAmount = findViewById(R.id.etExpenseAmount);
        etExpenseDateTime = findViewById(R.id.etExpenseDateTime);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        textViewExpenseTotal = findViewById(R.id.textViewExpenseTotal);
        textViewBalanceTotal = findViewById(R.id.textViewBalanceTotal);
        textViewDate = findViewById(R.id.textViewDate);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);

        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database helper
        expenseDbHelper = new ExpenseDbHelper(this);
        databaseHelper = new DatabaseHelper(this);

        // Set up the SaveExpense button click listener
        btnSaveExpense.setOnClickListener(view -> saveExpense());

        etExpenseParticulars.setOnLongClickListener(v -> {
            if(StringUtils.isBlank(etExpenseParticulars.getText())) {
                return false;
            }
            View view = getLayoutInflater().inflate(R.layout.other_entity, null, false);

            Button btnAddToBucket = view.findViewById(R.id.btnAddToBucket);
            ImageView btnDelSpinnerItem = view.findViewById(R.id.btnDelSpinnerItem);
            Spinner spinnerBucket = view.findViewById(R.id.spinnerBucket);
            EditText etItemName = view.findViewById(R.id.etItemName);
            EditText etItemValue = view.findViewById(R.id.etItemValue);

            List<String> items = new ArrayList<>();

            spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerBucket.setAdapter(spinnerAdapter);

            Map<String, Integer> itemMap = new HashMap<>();

            btnAddToBucket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String itemName = etItemName.getText().toString();
                    String itemValue = etItemValue.getText().toString();
                    if(itemName != null && !itemValue.isEmpty() && itemValue != null && !itemValue.isEmpty()){
                        addItemToSpinner(itemName, itemValue);
                        showCustomToast("Item " + itemName + " added");
//                        animateBackground(spinnerBucket , false);

                    }



                }




                private void addItemToSpinner(String itemName, String itemValue) {
                    items.clear();
                    itemMap.put(itemName, Integer.valueOf(itemValue));
                    itemMap.forEach((k,v) -> {
                        items.add(k+" = "+v);
                    });
                    etItemName.setText("");
                    etItemValue.setText("");
                    spinnerAdapter.notifyDataSetChanged();
                }
            });


            btnDelSpinnerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteSelectedItem();
                }

                private void deleteSelectedItem() {
                    int selectedItemPosition = spinnerBucket.getSelectedItemPosition();
                    String itemName = (String) spinnerBucket.getItemAtPosition(selectedItemPosition);

                    if (selectedItemPosition != AdapterView.INVALID_POSITION) {
                        items.remove(selectedItemPosition);
                        itemMap.remove(itemName.split(" = ")[0]);
                        spinnerAdapter.notifyDataSetChanged();
                        showCustomToast("Item "+ itemName.split(" = ")[0] +" removed");

                    }
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Expense Particulars");
            builder.setCancelable(false);
            builder.setIcon(R.drawable.expense);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ExpenseParticularsDto expenseParticularsDto = new ExpenseParticularsDto();
                    expenseParticularsDto.setExpenseParticulars(String.valueOf(etExpenseParticulars.getText()));
                    expenseParticularsDto.setExpensePriceMap(itemMap);

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(expenseParticularsDto);

                    System.err.println("MAP json >>> "+jsonString);


                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setView(view);

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        });

        etExpenseDateTime.setOnClickListener(v -> showDateTimeDialog());
        getAllExpenses(expenseRecyclerViewAdapter);

        expenseRecyclerViewAdapter = new ExpenseRecyclerViewAdapter(this, expenseItems);
        expenseRecyclerView.setAdapter(expenseRecyclerViewAdapter);


        LocalDate localDate = LocalDate.now();
        textViewDate.setText(localDate.getDayOfMonth()+"-"+String.format("%02d",localDate.getMonthValue())+"-"+localDate.getYear());

        Cursor res = expenseDbHelper.getTodaysTotalExpense(String.valueOf(localDate));

        if(res!=null && res.getCount()>0) {
            while(res.moveToNext()) {
                textViewExpenseTotal.setText(String.valueOf("\u20B9"+res.getInt(0)));
                textViewBalanceTotal.setText(String.valueOf("\u20B9"+res.getInt(1)));
            }
        }


    }

    private void showCustomToast(String message) {
        // Inflate the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        // Set the text for the toast
        TextView toastText = layout.findViewById(R.id.toast_text);
        toastText.setText(message);

        // Optionally update the icon if needed
        ImageView toastIcon = layout.findViewById(R.id.toast_icon);
//                 toastIcon.setImageResource(R.drawable.baseline_face_24);

        // Create and display the toast
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP|Gravity.CENTER,toast.getXOffset(), toast.getYOffset());
        toast.show();
    }


    // Handle back button click
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Navigate back to the previous activity
        return true;
    }

    private void getAllExpenses(ExpenseRecyclerViewAdapter expenseRecyclerViewAdapter) {
        expenseItems = expenseDbHelper.getAllExpenses(expenseRecyclerViewAdapter);
    }

    private void showDateTimeDialog() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(
                            this,
                            (TimePicker view1, int hourOfDay, int minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
//                                String dateTime = new SimpleDateFormat("dd").format(calendar.getTime()) + "-" + (String.format("%02d",month + 1) ) + "-" + year + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
//                                dateVal = year + "-" + new SimpleDateFormat("MM").format(calendar.getTime()) + "-" + new SimpleDateFormat("dd").format(calendar.getTime());
                                String dateTimeVal = year + "-" + (String.format("%02d",month + 1) ) + "-" + new SimpleDateFormat("dd").format(calendar.getTime()) + " " + new SimpleDateFormat("HH").format(calendar.getTime()) + ":" + new SimpleDateFormat("mm").format(calendar.getTime());
                                etExpenseDateTime.setText(dateTimeVal);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveExpense() {
        String particulars = etExpenseParticulars.getText().toString().trim();
        String amountStr = etExpenseAmount.getText().toString().trim();
        String datetime = etExpenseDateTime.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(particulars)) {
            etExpenseParticulars.setError("Please enter expense particulars");
            etExpenseParticulars.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            etExpenseAmount.setError("Please enter expense amount");
            etExpenseAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etExpenseAmount.setError("Invalid amount");
            etExpenseAmount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(datetime)) {
            etExpenseDateTime.setError("Please enter expense date and time");
            etExpenseDateTime.requestFocus();
            return;
        }
        String date = "";
        if(StringUtils.isNotEmpty(datetime)) {
            date = datetime.substring(0,10);
        }
        LocalDate yesterDay = LocalDate.now();
        if(StringUtils.isNotEmpty(date)) {
            LocalDate dateParsed = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            yesterDay = dateParsed.minusDays(1);
        }

        long yesterdaysBalance = getYesterdaysBalance(yesterDay);
        long todaysSales = getTodaysSales(date);
        long balance = 0L;
        balance = (yesterdaysBalance + todaysSales) - ((long) amount);
//        System.out.println("Yesterdays Bal: "+yesterdaysBalance+"\nTodays: "+ todaysSales+"\nBalance: "+balance);

        Cursor cursor = checkIfExpenseExists(date);
        boolean isInserted = false;
        if(cursor!=null && cursor.getCount()>0) {
            int idRow = 0,expAmountRow=0,yBalance=0,balanceRow = 0,yesterdaysBalanceRow = 0,todaysSalesRow=0;
            String particularsRow = "",expDateRow="",expDateTimeRow="",datetimeRow = "";
            while(cursor.moveToNext()) {
                idRow = cursor.getInt(0);
                particularsRow = cursor.getString(1);
//                expAmountRow = cursor.getInt(2);
                expDateTimeRow = cursor.getString(3);
                expDateRow = cursor.getString(4);
//                yesterdaysBalanceRow = cursor.getInt(5);
//                todaysSalesRow = cursor.getInt(6);
//                balanceRow = cursor.getInt(7);
            }
            isInserted = expenseDbHelper.insertExpense(idRow, particularsRow, amount, expDateTimeRow, expDateRow,yesterdaysBalance, todaysSales, balance);

            List<String> dates = getDatesFromNextDayToToday(date);

            dates.forEach(currDate -> {

                Cursor res = checkIfExpenseExists(currDate);
                if(res!=null && res.getCount()>0) {
                    while(res.moveToNext()) {
                        int expId = res.getInt(0);
                        int expAmount = res.getInt(2);
                        String expDate = res.getString(4);


                        LocalDate yesterDays = LocalDate.now();
                        if(StringUtils.isNotEmpty(currDate)) {
                            LocalDate dateParsed = LocalDate.parse(currDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            yesterDays = dateParsed.minusDays(1);
                        }

                        long yesterdaysBalanceUpdated = getYesterdaysBalance(yesterDays);
                        long todaysSalesUpdated = getTodaysSales(currDate);
                        long balanceUpdated = (yesterdaysBalanceUpdated + todaysSalesUpdated) - ((long) expAmount);

                        long rowsAffected = expenseDbHelper.updateExpenseYesterdaysBalanceAndSales(expId, expDate, yesterdaysBalanceUpdated, todaysSalesUpdated, balanceUpdated);

                        // Check if the update was successful
                        if (rowsAffected > 0) {
                            Toast.makeText(this, "Record updated successfully "+expId, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, expId+" Update failed. No matching record found.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });



        } else {
            isInserted = expenseDbHelper.insertExpense(null, particulars, amount, datetime, date,yesterdaysBalance, todaysSales, balance);
        }

        if (isInserted) {
            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();
            Cursor res = expenseDbHelper.getTodaysTotalExpense(String.valueOf(LocalDate.now()));

            if(res!=null && res.getCount()>0) {
                while(res.moveToNext()) {
                    textViewExpenseTotal.setText(String.valueOf("\u20B9"+res.getInt(0)));
                    textViewBalanceTotal.setText(String.valueOf("\u20B9"+res.getInt(1)));
                }
            }
        } else {
            Toast.makeText(this, "Failed to save expense. Please try again.", Toast.LENGTH_SHORT).show();
        }
        getAllExpenses(expenseRecyclerViewAdapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getDatesFromNextDayToToday(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(inputDate, formatter).plusDays(1); // Start from next day
        LocalDate today = LocalDate.now();

        List<String> dateList = new ArrayList<>();
        while (!startDate.isAfter(today)) {
            dateList.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }

        return dateList;
    }

    private Cursor checkIfExpenseExists(String date) {
        return expenseDbHelper.checkIfExpenseExists(date);
    }

    private long getTodaysSales(String date) {
        return databaseHelper.getTodaysSales(date);
    }

    private Long getYesterdaysBalance(LocalDate yesterDay) {
        //get yesterdays balance
        return expenseDbHelper.getYesterdaysBalance(String.valueOf(yesterDay));
    }

    // Clear the input fields after saving
    private void clearInputs() {
        etExpenseParticulars.setText("");
        etExpenseAmount.setText("");
        etExpenseDateTime.setText("");
    }
}