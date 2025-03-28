package com.example.myapplication.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import android.content.ContentValues;

import com.example.myapplication.ExpenseActivity;
import com.example.myapplication.ExpenseRecyclerView;
import com.example.myapplication.ExpenseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

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

    public ExpenseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
}

