package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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

import com.example.myapplication.adapters.Expense;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.database.ExpenseDbHelper;
import com.example.myapplication.dtos.ExpenseParticularsDto;
import com.example.myapplication.utils.JsonUtils;
import com.example.myapplication.utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ExpenseActivity extends AppCompatActivity {

    private EditText etExpenseParticulars, etExpenseAmount, etExpenseDateTime;
    private Button btnSaveExpense,getExpenses;
    private ExpenseDbHelper expenseDbHelper;
    private DatabaseHelper databaseHelper;
    private TextView textViewExpenseTotal,textViewBalanceTotal,textViewDate;
    private RecyclerView expenseRecyclerView;
    private ExpenseRecyclerViewAdapter expenseRecyclerViewAdapter;
    public static List<ExpenseRecyclerView> expenseItems = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private String expParticularsJson = null;
    private Menu menu;


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
        getExpenses = findViewById(R.id.getExpenses);
        textViewExpenseTotal = findViewById(R.id.textViewExpenseTotal);
        textViewBalanceTotal = findViewById(R.id.textViewBalanceTotal);
        textViewDate = findViewById(R.id.textViewDate);
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);

        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database helper
        expenseDbHelper = new ExpenseDbHelper(this);
        databaseHelper = new DatabaseHelper(this);

        getExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = expenseDbHelper.getAllExpenses();

                if(cursor !=null && cursor.getCount()>0) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ExpenseActivity.this);
                    builder.setTitle("Expense details");
                    builder.setCancelable(false);
                    builder.setIcon(R.drawable.ic_report);
                    View customView = getLayoutInflater().inflate(R.layout.expense_table_layout, null, false);


                    ImageView profileImageView = customView.findViewById(R.id.profileImageView);

                    Bitmap profileBitmap = generateInitialsImage("Rohit", "Kurbetti", 80, ExpenseActivity.this);
                    profileImageView.setImageBitmap(profileBitmap);


                    builder.setView(customView);

                    TableLayout tableLayout = customView.findViewById(R.id.tableLayout);

                    // Create new TableRow
                    TableRow tableHeaderRow = new TableRow(ExpenseActivity.this);
                    tableHeaderRow.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));


                    populateHeaderRow(tableLayout, tableHeaderRow);


                    while(cursor.moveToNext()) {
                        int id = cursor.getInt(0);
                        String expPart = cursor.getString(1);
                        int expAmt = cursor.getInt(2);
                        String expDateTime = cursor.getString(3);
                        String expDate = cursor.getString(4);
                        int yBalance = cursor.getInt(5);
                        int sales = cursor.getInt(6);
                        int balance = cursor.getInt(7);

//                        Expense expense = new Expense(id, expPart, expAmt, expDateTime, expDate, yBalance, sales, balance);

                        // Create new TableRow
                        TableRow tableRow = new TableRow(ExpenseActivity.this);
                        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));

                        // Create TextViews to add to the row
                        TextView col1 = new TextView(ExpenseActivity.this);
                        col1.setText(String.valueOf(id));
                        col1.setPadding(16, 16, 16, 16);
                        col1.setGravity(Gravity.CENTER);

                        TextView col2 = new TextView(ExpenseActivity.this);
                        col2.setText(String.valueOf(expPart));
                        col2.setPadding(16, 16, 16, 16);
                        col2.setGravity(Gravity.CENTER);

                        TextView col3 = new TextView(ExpenseActivity.this);
                        col3.setText(String.valueOf(expAmt));
                        col3.setPadding(16, 16, 16, 16);
                        col3.setGravity(Gravity.CENTER);


                        TextView col4 = new TextView(ExpenseActivity.this);
                        col4.setText(String.valueOf(expDateTime));
                        col4.setPadding(16, 16, 16, 16);
                        col4.setGravity(Gravity.CENTER);


                        TextView col5 = new TextView(ExpenseActivity.this);
                        col5.setText(String.valueOf(expDate));
                        col5.setPadding(16, 16, 16, 16);
                        col5.setGravity(Gravity.CENTER);


                        TextView col6 = new TextView(ExpenseActivity.this);
                        col6.setText(String.valueOf(yBalance));
                        col6.setPadding(16, 16, 16, 16);
                        col6.setGravity(Gravity.CENTER);


                        TextView col7 = new TextView(ExpenseActivity.this);
                        col7.setText(String.valueOf(sales));
                        col7.setPadding(16, 16, 16, 16);
                        col7.setGravity(Gravity.CENTER);


                        TextView col8 = new TextView(ExpenseActivity.this);
                        col8.setText(String.valueOf(balance));
                        col8.setPadding(16, 16, 16, 16);
                        col8.setGravity(Gravity.CENTER);


                        // Add TextViews to TableRow
                        tableRow.addView(col1);
                        tableRow.addView(col2);
                        tableRow.addView(col3);
                        tableRow.addView(col4);
                        tableRow.addView(col5);
                        tableRow.addView(col6);
                        tableRow.addView(col7);
                        tableRow.addView(col8);

                        // Add TableRow to TableLayout
                        tableLayout.addView(tableRow);

                    }


                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }

            }

            private void populateHeaderRow(TableLayout tableLayout, TableRow tableHeaderRow) {

                // Create TextViews to add to the row
                TextView col1 = new TextView(ExpenseActivity.this);
                col1.setText(String.valueOf("Id"));
                col1.setTypeface(null, Typeface.BOLD); // Set text as bold
                col1.setPadding(16, 16, 16, 16);
                col1.setGravity(Gravity.CENTER);

                TextView col2 = new TextView(ExpenseActivity.this);
                col2.setText(String.valueOf("Particulars"));
                col2.setTypeface(null, Typeface.BOLD); // Set text as bold
                col2.setPadding(16, 16, 16, 16);
                col2.setGravity(Gravity.CENTER);

                TextView col3 = new TextView(ExpenseActivity.this);
                col3.setTypeface(null, Typeface.BOLD); // Set text as bold
                col3.setText(String.valueOf("Amount"));
                col3.setPadding(16, 16, 16, 16);
                col3.setGravity(Gravity.CENTER);


                TextView col4 = new TextView(ExpenseActivity.this);
                col4.setTypeface(null, Typeface.BOLD); // Set text as bold
                col4.setText(String.valueOf("Date Time"));
                col4.setPadding(16, 16, 16, 16);
                col4.setGravity(Gravity.CENTER);


                TextView col5 = new TextView(ExpenseActivity.this);
                col5.setTypeface(null, Typeface.BOLD); // Set text as bold
                col5.setText(String.valueOf("Date"));
                col5.setPadding(16, 16, 16, 16);
                col5.setGravity(Gravity.CENTER);


                TextView col6 = new TextView(ExpenseActivity.this);
                col6.setTypeface(null, Typeface.BOLD); // Set text as bold
                col6.setText(String.valueOf("Yesterdays Balance"));
                col6.setPadding(16, 16, 16, 16);
                col6.setGravity(Gravity.CENTER);


                TextView col7 = new TextView(ExpenseActivity.this);
                col7.setTypeface(null, Typeface.BOLD); // Set text as bold
                col7.setText(String.valueOf("Sales"));
                col7.setPadding(16, 16, 16, 16);
                col7.setGravity(Gravity.CENTER);


                TextView col8 = new TextView(ExpenseActivity.this);
                col8.setTypeface(null, Typeface.BOLD); // Set text as bold
                col8.setText(String.valueOf("Balance"));
                col8.setPadding(16, 16, 16, 16);
                col8.setGravity(Gravity.CENTER);

                // Add TextViews to TableRow
                tableHeaderRow.addView(col1);
                tableHeaderRow.addView(col2);
                tableHeaderRow.addView(col3);
                tableHeaderRow.addView(col4);
                tableHeaderRow.addView(col5);
                tableHeaderRow.addView(col6);
                tableHeaderRow.addView(col7);
                tableHeaderRow.addView(col8);

                // Add TableRow to TableLayout
                tableLayout.addView(tableHeaderRow);

            }
        });


        // Set up the SaveExpense button click listener
        btnSaveExpense.setOnClickListener(view -> {
            try {
                saveExpense();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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
                    expParticularsJson = jsonString;
                    AtomicInteger totalExpense = new AtomicInteger();
                    itemMap.forEach((k,v)->{
                        totalExpense.addAndGet(v);
                    });

                    etExpenseAmount.setText(String.valueOf(totalExpense.get()));
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

    public static Bitmap generateInitialsImage(String firstName, String lastName, int sizeInDp, Context context) {
        String initials = "";
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.substring(0, 1).toUpperCase();
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.substring(0, 1).toUpperCase();
        }

        int sizeInPx = (int) (sizeInDp * context.getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(sizeInPx, sizeInPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw circle background
        Paint circlePaint = new Paint();

        int[] colors = {
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#3F51B5"), // Indigo
                Color.parseColor("#03A9F4"), // Light Blue
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#795548")  // Brown
        };

        Random random = new Random();
        int randomColor = colors[random.nextInt(colors.length)];
        circlePaint.setColor(randomColor);

        circlePaint.setAntiAlias(true);
        canvas.drawCircle(sizeInPx / 2, sizeInPx / 2, sizeInPx / 2, circlePaint);

        // Draw text (initials)
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(sizeInPx / 2); // Adjust text size
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float x = sizeInPx / 2f;
        float y = sizeInPx / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2;

        canvas.drawText(initials, x, y, textPaint);

        return bitmap;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_section, menu);  // inflate the menu only for this activity
        this.menu = menu;

        List<Expense> allExpenses = expenseDbHelper.getAllExpenseParsedList();

//        List<Expense> fltrExp = allExpenses.stream().filter(expense -> expense.getYesterdaysBalance()==0).collect(Collectors.toList());

        if(!allExpenses.isEmpty()) {
            List<LocalDate> missing = findMissingDates(allExpenses);

            if (missing.isEmpty()) {
                toggleMenuItem(false);
            } else {
                toggleMenuItem(true);
            }
            Utils.showMissingDatesPopup(this, missing);
        }
        return true;
    }

    private void toggleMenuItem(boolean enable) {
        if(menu != null) {
            MenuItem item = menu.findItem(R.id.action_check_missing_expenses);
            if (item != null) {
                item.setVisible(enable);
                item.setIcon(R.drawable.warning);
//                item.getIcon().setAlpha(enable ? 255 : 100); // Optional: visually fade icon when disabled
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_check_missing_expenses) {

            List<Expense> missingExpenses = expenseDbHelper.getAllExpenseParsedList();

            if(!missingExpenses.isEmpty()) {
                List<LocalDate> missing = findMissingDates(missingExpenses);
                Utils.showMissingDatesPopup(this, missing);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    private void saveExpense() throws IOException {
        String particulars = etExpenseParticulars.getText().toString().trim();

        if(StringUtils.isNotBlank(expParticularsJson)) {
            particulars = expParticularsJson;
        }

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
        String date;
        if(StringUtils.isNotEmpty(datetime)) {
            date = datetime.substring(0,10);
        } else {
            date = "";
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
        Expense expense = null;
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
            isInserted = expenseDbHelper.insertExpense(idRow, particulars, amount, datetime, date,yesterdaysBalance, todaysSales, balance);

            expense = new Expense(idRow, particulars, (int) amount, datetime, date, (int) yesterdaysBalance, (int) todaysSales, (int) balance);




        } else {
            isInserted = expenseDbHelper.insertExpense(null, particulars, amount, datetime, date,yesterdaysBalance, todaysSales, balance);

            expense = new Expense(null, particulars, (int) amount, datetime, date, (int) yesterdaysBalance, (int) todaysSales, (int) balance);

        }

        List<String> dates = getDatesFromNextDayToToday(date);

        dates.forEach(currDate -> {

            Cursor res = checkIfExpenseExists(currDate);
            if(res!=null && res.getCount()>0) {
                while(res.moveToNext()) {
                    int expId = res.getInt(0);
                    String expPart = res.getString(1);
                    int expAmount = res.getInt(2);
                    String expDateTime = res.getString(3);
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
                        Expense exp = new Expense(expId, expPart, expAmount, expDateTime, expDate, (int) yesterdaysBalanceUpdated, (int) todaysSalesUpdated, (int) balanceUpdated);
                        saveExpenseOnCloud(this, expDate, exp);
                    } else {
                        Toast.makeText(this, expId+" Update failed. No matching record found.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        if (isInserted) {
            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
            clearInputs();

            //save expense on cloud

            saveExpenseOnCloud(this, date, expense);


            Cursor res = expenseDbHelper.getTodaysTotalExpense(String.valueOf(LocalDate.now()));

            if(res!=null && res.getCount()>0) {
                while(res.moveToNext()) {
                    textViewExpenseTotal.setText(String.valueOf("\u20B9"+res.getInt(0)));
                    textViewBalanceTotal.setText(String.valueOf("\u20B9"+res.getInt(1)));
                }
            }

            writeAllExpenseContentInTxtFile();

        } else {
            Toast.makeText(this, "Failed to save expense. Please try again.", Toast.LENGTH_SHORT).show();
        }
        getAllExpenses(expenseRecyclerViewAdapter);

        List<Expense> missingExpenses = expenseDbHelper.getAllExpenseParsedList();


        List<LocalDate> missing = findMissingDates(missingExpenses);

        if(missing.isEmpty()){
            toggleMenuItem(false);
        } else {
            toggleMenuItem(true);
            Utils.showMissingDatesPopup(this, missing);
        }

    }

    private void writeAllExpenseContentInTxtFile() throws IOException {
        List<Expense> expenseList = expenseDbHelper.getAllExpenseParsedList();

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "ExpensesBackup.csv";

        FileWriter writer = new FileWriter(filePath);

        expenseList.forEach(expense -> {

            int id = expense.getId();
            String expensePart = expense.getExpensePart();
            if(JsonUtils.isValidJson(expensePart)) {
                expensePart = "\""+expensePart+"\"";
            }


            int expenseAmount = expense.getExpenseAmount();
            String expenseExpenseDateTime = expense.getExpenseDateTime();
            String expenseDate = expense.getExpenseDate();
            int expenseYesterdaysBalance = expense.getYesterdaysBalance();
            int expenseSales = expense.getSales();
            int expenseBalance = expense.getBalance();


            // Construct CSV row
            String csvRow = id + "," +  expensePart + "," + expenseAmount + "," + expenseExpenseDateTime+ "," + expenseDate+"," + expenseYesterdaysBalance+"," + expenseSales+"," + expenseBalance+ "\n";

            // Write CSV row to file
            try {
                writer.append(csvRow);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


        // Close CSV writer
        writer.flush();
        writer.close();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void saveExpenseOnCloud(Context context, String date, Expense expense) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        SharedPreferences sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        String deviceModel = sharedPreferences.getString("model", Build.MODEL);

        DatabaseReference expRef = database.getReference(deviceModel+"/"+"expenses");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate localDate = LocalDate.parse(date, dateTimeFormatter);

        boolean todaysDate = true;
        if(localDate.equals(LocalDate.now())) {
            todaysDate = true;
        } else {
            todaysDate = false;
        }

        int year = localDate.getYear();
        Month month = localDate.getMonth();
        int day = localDate.getDayOfMonth();

        String monthShort = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // e.g., "Dec"

        String sendLocalDate = LocalDate.now().toString();
//        String sendLocalDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"));

        // Store user data
        expRef.child(year+"").child(monthShort+"-"+year).child(todaysDate ==true ? sendLocalDate : localDate+"")
                .setValue(expense)
                .addOnSuccessListener(aVoid -> {
                    // Data stored successfully
                    Toast.makeText(context, "Expense saved on cloud successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to store data
                    Toast.makeText(context, "Failed to save expense data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<LocalDate> findMissingDates(List<Expense> dateStrings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<LocalDate> dateSet = new HashSet<>();
        // Parse strings to LocalDate and store in a set
        for (Expense dateStr : dateStrings) {
            dateSet.add(LocalDate.parse(dateStr.getExpenseDate(), formatter));
        }

        // Find the min and max dates
        LocalDate minDate = Collections.min(dateSet);
        LocalDate maxDate = Collections.max(dateSet);

        List<LocalDate> missingDates = new ArrayList<>();

        // Loop through the range and check for missing dates
        for (LocalDate date = minDate; !date.isAfter(maxDate); date = date.plusDays(1)) {
            if (!dateSet.contains(date)) {

                LocalDate finalDate = date;
                boolean isExists = dateStrings.stream().anyMatch(ex -> ex.getExpenseDate().contains(String.valueOf(finalDate)));
                List<Expense> tempExp = new ArrayList<>();
                if(isExists) {
                    tempExp = dateStrings.stream().filter(expense -> expense.getExpenseDate().equalsIgnoreCase(String.valueOf(finalDate)))
                            .filter(exp -> exp.getYesterdaysBalance()>0).collect(Collectors.toList());
                    System.err.println(" missing dates >> "+tempExp.size());
                } else {
                    missingDates.add(date);
                    System.err.println(" missing dates. >> "+tempExp.size());
                }
            }
        }
        return missingDates;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getDatesFromNextDayToToday(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(inputDate, formatter).plusDays(1); // Start from next day

        String maxDate = expenseDbHelper.getMaxDateFromExpenses();
        LocalDate today = LocalDate.parse(maxDate, formatter);


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