package com.example.myapplication.database;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import android.content.ContentValues;
import android.os.Build;
import android.util.Log;

import com.example.myapplication.ExpenseActivity;
import com.example.myapplication.ExpenseRecyclerView;
import com.example.myapplication.ExpenseRecyclerViewAdapter;
import com.example.myapplication.adapters.Expense;
import com.example.myapplication.dtos.Invoice;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class ExpenseDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PARTICULARS = "expenseParticulars";
    public static final String COLUMN_AMOUNT = "expenseAmount";
    public static final String COLUMN_DATETIME = "expenseDateTime";
    public static final String COLUMN_DATE = "expenseDate";
    public static final String COLUMN_YESTERDAYS_BALANCE = "yesterdaysBalance";
    public static final String COLUMN_SALES = "sales";
    public static final String COLUMN_BALANCE = "balance";
    private final Context context;

    public ExpenseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EXPENSES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PARTICULARS + " TEXT, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_DATETIME + " TEXT,"
                + COLUMN_DATE + " TEXT, "
                + COLUMN_YESTERDAYS_BALANCE + " INTEGER, "
                + COLUMN_SALES + " INTEGER, "
                + COLUMN_BALANCE + " INTEGER "
                + " )";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exists and create new table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    // Method to insert expense into database
    public boolean insertExpense(Integer id, String particulars, double amount, String datetime, String date, long yesterdaysBalance, long todaysSales, long balance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long result = 0;
        values.put(COLUMN_PARTICULARS, particulars);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATETIME, datetime);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_YESTERDAYS_BALANCE, yesterdaysBalance);
        values.put(COLUMN_SALES, todaysSales);
        values.put(COLUMN_BALANCE, balance);
        if(id != null) {
            values.put(COLUMN_ID, id);
            result = db.update(TABLE_EXPENSES, values,"id=?",new String[] {String.valueOf(id)});
        } else {
            result = db.insert(TABLE_EXPENSES, null, values);
        }
        db.close();
        return result != -1;
    }

    public Cursor getTodaysTotalExpense(String todayDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT expenseAmount,balance FROM " + TABLE_EXPENSES + " where expenseDate = ?";
        Cursor cursor = db.rawQuery(query, new String[]{todayDate});
        return cursor;
    }

    public long getYesterdaysBalance(String yesterDay) {
        long balance = 0L;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "+ COLUMN_BALANCE +" FROM "+ TABLE_EXPENSES + " where "+COLUMN_DATE+"=?";
        Cursor cursor = db.rawQuery(query, new String[]{yesterDay});
        if(cursor.getCount()>0) {
            while(cursor.moveToNext()) {
                balance = Long.parseLong(String.valueOf(cursor.getInt(0)));
            }
        }
        return balance;
    }

    public Cursor checkIfExpenseExists(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_EXPENSES + " where "+COLUMN_DATE+"=?";
        Cursor cursor = db.rawQuery(query, new String[]{date});
        if(cursor.getCount()>0) {
            return cursor;
        }
        return null;
    }

    public boolean checkIfExpenseExistsFlag(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_EXPENSES + " where "+COLUMN_DATE+"=?";
        Cursor cursor = db.rawQuery(query, new String[]{date});
        if(cursor.getCount()>0) {
            return true;
        }
        return false;
    }

    public List<ExpenseRecyclerView> getAllExpenses(ExpenseRecyclerViewAdapter expenseRecyclerViewAdapter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_EXPENSES + " order by id desc";
        Cursor cursor = db.rawQuery(query, null);

        List<ExpenseRecyclerView> expenseRecyclerViewList = new ArrayList<>();

        if(cursor!=null && cursor.getCount()>0) {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String part = cursor.getString(1);
                int expAmount = cursor.getInt(2);
                String expDate = cursor.getString(4);
                int sales = cursor.getInt(6);

                ExpenseRecyclerView item = new ExpenseRecyclerView(id, part, expAmount, expDate, sales);
                expenseRecyclerViewList.add(item);

            }
        }
        if(expenseRecyclerViewAdapter != null && !expenseRecyclerViewList.isEmpty()){
            ExpenseActivity.expenseItems.clear();
            ExpenseActivity.expenseItems.addAll(expenseRecyclerViewList);
            expenseRecyclerViewAdapter.notifyDataSetChanged();
        }
        return expenseRecyclerViewList;
    }

    public Cursor getAllExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + TABLE_EXPENSES + " order by "+COLUMN_DATE+" desc ", null);
    }


    public void deleteExpensesByIds(Set<Integer> expenseIds) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (expenseIds == null || expenseIds.isEmpty()) {
            return; // No IDs to delete
        }

        // Create a placeholder string for IN clause (?, ?, ?)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < expenseIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ", ?");
        }

        // Build the final SQL query
        String sql = "DELETE FROM "+ TABLE_EXPENSES +" WHERE id IN (" + placeholders + ")";
        Log.d(">>",expenseIds+" "+sql);
        // Convert Set<Integer> to String[] for binding arguments
        String[] args = expenseIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        // Execute the delete query
        db.execSQL(sql, args);
        db.close();
    }

    public Cursor getExpenseByDate(String expDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSES + " where expenseDate = ?";
        Cursor cursor = db.rawQuery(query, new String[]{expDate});
        return cursor;
    }

    public long updateExpenseAfterNewInvoices(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long result = 0;
        int id = expense.getId();
        values.put(COLUMN_PARTICULARS, expense.getExpensePart());
        values.put(COLUMN_AMOUNT, expense.getExpenseAmount());
        values.put(COLUMN_DATETIME, expense.getExpenseDateTime());
        values.put(COLUMN_DATE, expense.getExpenseDate());
        values.put(COLUMN_YESTERDAYS_BALANCE, expense.getYesterdaysBalance());
        values.put(COLUMN_SALES, expense.getSales());
        values.put(COLUMN_BALANCE, expense.getBalance());
        values.put(COLUMN_ID, id);
            result = db.update(TABLE_EXPENSES, values,"id=?",new String[] {String.valueOf(id)});
        db.close();
        return result;
    }

    public long updateExpenseYesterdaysBalanceAndSales(int expId, String expDate, long yesterdaysBalanceUpdated, long todaysSalesUpdated, long balanceUpdated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        long rowsAffected = 0;

        values.put("yesterdaysBalance", yesterdaysBalanceUpdated);
        values.put("sales", todaysSalesUpdated);
        values.put("balance", balanceUpdated);

        // Update query
        rowsAffected = db.update(TABLE_EXPENSES, values, "id = ? AND expenseDate = ?",
                new String[]{String.valueOf(expId), expDate});



        return rowsAffected;
    }

    public String getMaxDateFromExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX("+ COLUMN_DATE +") FROM " + TABLE_EXPENSES;
        Cursor cursor = db.rawQuery(query, null);
        String maxDate = null;
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                 maxDate = cursor.getString(0);
            }
        }
        return maxDate;
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, null, null);
        db.close();
    }

    public List<String> checkForMissingExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> missingExpDates = new ArrayList<>();
        String query = "SELECT "+ COLUMN_DATE +" FROM " + TABLE_EXPENSES + " where yesterdaysBalance <= 0 ";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                missingExpDates.add(cursor.getString(0));
            }
        }
        return missingExpDates;
    }

    public List<Expense> getAllExpenseParsedList() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_EXPENSES + " order by id desc";
        Cursor cursor = db.rawQuery(query, null);

        List<Expense> expenseList = new ArrayList<>();

        if(cursor!=null && cursor.getCount()>0) {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String part = cursor.getString(1);
                int expAmount = cursor.getInt(2);
                String expDateTime = cursor.getString(3);
                String expDate = cursor.getString(4);
                int yesterdaysBalance = cursor.getInt(5);
                int sales = cursor.getInt(6);
                int balance = cursor.getInt(7);

                expenseList.add(new Expense(id, part, expAmount, expDateTime, expDate, yesterdaysBalance, sales, balance));

            }
        }
        return expenseList;
    }

    public List<String> getMissingInvoicesParsedList() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> missingExpDates = new ArrayList<>();
        String query = "SELECT "+ COLUMN_DATE +" FROM " + TABLE_EXPENSES + " where sales = 0 ";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                missingExpDates.add(cursor.getString(0));
            }
        }
        return missingExpDates;
    }

    public String findMinExpDate() {
        String minDate = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MIN("+ COLUMN_DATE +") FROM " + TABLE_EXPENSES;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                minDate = cursor.getString(0);
            }
        }
        return minDate;
    }

    public void deleteFirestoreExpensesbyIds(List<Expense> expenseList, ProgressDialog progressDialog) {
        progressDialog.setMessage("Deleting expenses from cloud database");
        progressDialog.show();
        List<Expense> selExpIds = expenseList.stream().filter(Expense::getChecked).collect(Collectors.toList());

        if(!selExpIds.isEmpty()) {

            selExpIds.forEach(expense -> {
                String date = expense.getExpenseDate();

                String formattedOnlyYear = formatOnlyYear(date);  // 2025
                String formattedMonthYear = formatMonthYear(date);  // Mar-2025

                SharedPreferences sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
                String deviceModel = sharedPreferences.getString("model", Build.MODEL);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel+"/"+"expenses");
                databaseReference.child("/"+ formattedOnlyYear +"/"+ formattedMonthYear +"/"+ date +"/").removeValue()
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                        });
            });

        } else {
            progressDialog.dismiss();
        }
    }

    private String formatMonthYear(String inputDate) {
        try {
            // Define input date format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            // Define output format as "MMM-yyyy"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM-yyyy", Locale.US);

            // Parse the input date
            Date parsedDate = inputFormat.parse(inputDate);

            // Format and return the result
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }

    private String formatOnlyYear(String inputDate) {
        try {
            // Define input date format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            // Define output format as "MMM-yyyy"
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy", Locale.US);

            // Parse the input date
            Date parsedDate = inputFormat.parse(inputDate);

            // Format and return the result
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }
}

