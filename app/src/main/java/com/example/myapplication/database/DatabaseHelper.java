package com.example.myapplication.database;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.myapplication.ExpenseActivity;
import com.example.myapplication.adapters.Expense;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.dtos.Invoice;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invoices.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SHARED_PREFS_FILE = "my_shared_prefs";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE invoices (" +
                "invoice_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "item_list_json TEXT," +
                "total BIGINT," +
                "created_date_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "created_date DATE DEFAULT CURRENT_DATE" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS invoices");
        onCreate(db);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long saveInvoiceTransaction(String itemListJson, Long grandTotal, Context context, DtoJson dtoJson, Long invoiceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(invoiceId!=null) {
            values.put("invoice_id", invoiceId);

        }
        values.put("total", grandTotal);
        dtoJson.setTotal(grandTotal);
        if(itemListJson.charAt(0)== '"' && itemListJson.charAt(itemListJson.length()-1)=='"') {
            itemListJson = itemListJson.substring(1,itemListJson.length()-1);
        }

        values.put("item_list_json", itemListJson);
        values.put("created_date_time", String.valueOf(dtoJson.getCreateddtm()));
        values.put("created_date", String.valueOf(dtoJson.getDate()));

        long newRowId = db.insert("invoices", null, values);
        if (newRowId != -1) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//            if(sharedPreferences.getBoolean("toggleCloudStore", true)) {
                putDataFireStore(context, values, newRowId, dtoJson, itemListJson);
//            }

            long newTodaysSales = getTodaysSales(dtoJson.getDate());

            //update expenses entry in sqlite db
            try (ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(context)) {
                Cursor res = expenseDbHelper.getExpenseByDate(values.getAsString("created_date"));

                if(res.getCount()>0) {
                    while (res.moveToNext()) {
                        int expId = res.getInt(0);
                        String expPart = res.getString(1);
                        int expAmount = res.getInt(2);
                        String expDateTime = res.getString(3);
                        String expDate = res.getString(4);

                        LocalDate yesterDay = LocalDate.now();
                        if(StringUtils.isNotEmpty(expDate)) {
                            LocalDate dateParsed = LocalDate.parse(expDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            yesterDay = dateParsed.minusDays(1);
                        }
                        long yesterdaysBalance = expenseDbHelper.getYesterdaysBalance(String.valueOf(yesterDay));

                        int yBalance = (int) yesterdaysBalance;
                        int newBalance = (int) ((newTodaysSales + yBalance) - expAmount);

                        //update the expense entry in EXPENSES table in db
                        Expense expense = new Expense(expId, expPart, expAmount, expDateTime, expDate, yBalance, (int) newTodaysSales, newBalance);

                        long updateRes = expenseDbHelper.updateExpenseAfterNewInvoices(expense);

                        if(updateRes>0) {
                            Toast.makeText(context, "Expenses updated", Toast.LENGTH_SHORT).show();
                            ExpenseActivity.saveExpenseOnCloud(context, expDate, expense);
                        } else {
                            Toast.makeText(context, "Expenses updation failed", Toast.LENGTH_SHORT).show();
                        }


                    }
                }



            }

        } else {
            Toast.makeText(context, "Invoice insertion failed", Toast.LENGTH_LONG).show();
        }
        return newRowId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void putDataFireStore(Context context, ContentValues values, long newRowId, DtoJson dtoJson, String itemListJson) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        Map<String, Object> map = new HashMap<>();
//        map.put("name","Rohit");
//        map.put("age",21);
//        db.("users")
//                .add(map)
//                .addOnSuccessListener(documentReference -> Log.d(">>>", "DocumentSnapshot added with ID: " + documentReference.getId()))
//                .addOnFailureListener(e -> Log.w(">>>", "Error adding document", e));
//        Toast.makeText(context, "Stored in firstore", Toast.LENGTH_SHORT).show();
//

        // Access a Cloud Firestore instance
//        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Create a new user document with a generated ID
//
//        DocumentReference newUserRef = db.collection("invoices")
//                .document(String.valueOf(LocalDate.now()))
//                .collection("subinvoices")
//                .document(LocalDateTime.now().getHour()+"_"+LocalDateTime.now().getMinute()+"_"+LocalDateTime.now().getSecond());
//
//        // Create a Map to represent the data
//        Map<String, Object> user = new HashMap<>();
//        user.put("invoice_id" , newRowId);
//        user.put("item_list_json" , values.get("item_list_json"));
//        user.put("total", values.get("total"));
//        user.put("created_date_time", values.get("created_date_time"));
//        user.put("created_date", values.get("created_date"));
//
//        // Add the data to Firestore
//        newUserRef.set(user)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "Invoice uploaded on cloud: "+newRowId, Toast.LENGTH_LONG).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Invoice upload failed!", Toast.LENGTH_SHORT).show();
//                });


        //Add the data to Realtime database

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        String deviceModel = sharedPreferences.getString("model", Build.MODEL);
        DatabaseReference myRef = database.getReference(deviceModel+"/"+"invoices");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");



        LocalDate localDate = LocalDate.parse(dtoJson.getDate(), dateTimeFormatter);
        Calendar calendar = Calendar.getInstance();

        int hour =calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        String datTimeStr = dtoJson.getCreateddtm();

        DateTimeFormatter dateTimeStrFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime ldtmParsed = LocalDateTime.parse(datTimeStr, dateTimeStrFormatter);

        hour = ldtmParsed.getHour();
        min = ldtmParsed.getMinute();
        sec = ldtmParsed.getSecond();

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

        DtoJsonEntity dtoJsonEntity = new DtoJsonEntity(newRowId,
                dtoJson.getName(),
                dtoJson.getTotal(),
                dtoJson.getCreateddtm(),
                dtoJson.getDate(),
                itemListJson);

        String sendLocalDate = LocalDate.now().toString();
        String sendLocalDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss"));

        // Store user data
        myRef.child(year+"").child(monthShort+"-"+year).child(todaysDate ==true ? sendLocalDate : localDate+"")
                .child(String.format("%02d",hour)+"_"+String.format("%02d",min)+"_"+String.format("%02d",sec)).setValue(dtoJsonEntity)
                .addOnSuccessListener(aVoid -> {
                    // Data stored successfully
                    Toast.makeText(context, "Data saved on cloud successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to store data
                    Toast.makeText(context, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });





    }

    public void getAllDbRecords(DtoJson dtoJson, Context context) throws IOException {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from invoices", null);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "InvoicesBackup.csv";

        FileWriter writer = new FileWriter(filePath);


        // Iterate through the cursor to retrieve data
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String column1 = cursor.getString(0);
                String column2 = cursor.getString(1);
                column2 = "\""+column2+"\"";
                String column3 = cursor.getString(2);
                String column4 = cursor.getString(3);
                String column5 = cursor.getString(4);

                // Construct CSV row
                String csvRow = column1 + "," +  column2 + "," + column3 + "," + column4+ "," + column5+ "\n";

                // Write CSV row to file
                writer.append(csvRow);
            }
            cursor.close();
        }

        // Close CSV writer
        writer.flush();
        writer.close();
    }

    public Cursor getAllInvoices() throws IOException {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from invoices order by invoice_id desc ", null);
    }

        public void deleteFireStoreData(Context context, ProgressDialog pd) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection("invoices");

        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            document.getReference().delete();
                        }
                        Toast.makeText(context, "Collection deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Collection is already empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error deleting collection: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public Cursor getPeriodRecords(String fromDate, String toDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM invoices WHERE created_date >= ?  and  created_date <= ? ";
        return db.rawQuery(query, new String[]{fromDate, toDate});
    }

    public Cursor getRecordById(String id, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM invoices WHERE invoice_id = ?";
        return db.rawQuery(query, new String[]{id});
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("invoices", null, null);
        db.close();
    }

    public long getTodaysSales(String date) {
        Long todaysSales = 0L;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT sum(total) FROM invoices WHERE created_date = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                todaysSales = Long.parseLong(String.valueOf(cursor.getLong(0)));
            }
        }
        return todaysSales;
    }

    public void deleteInvoicesbyIds(Set<Integer> invoiceIds) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (invoiceIds == null || invoiceIds.isEmpty()) {
            return; // No IDs to delete
        }

        // Create a placeholder string for IN clause (?, ?, ?)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < invoiceIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ", ?");
        }

        // Build the final SQL query
        String sql = "DELETE FROM invoices WHERE invoice_id IN (" + placeholders + ")";
        Log.d(">>",invoiceIds+" "+sql);
        // Convert Set<Integer> to String[] for binding arguments
        String[] args = invoiceIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        // Execute the delete query
        db.execSQL(sql, args);
        db.close();
    }

    public void deleteFirestoreInvoicesbyIds(List<Invoice> invoices, ProgressDialog progressDialog) {

        progressDialog.setMessage("Deleting from cloud database");
        progressDialog.show();
        List<Invoice> selInvs = invoices.stream().filter(Invoice::getChecked).collect(Collectors.toList());

        if(!selInvs.isEmpty()) {

            selInvs.forEach(invoice -> {
                String date = invoice.getCreatedDate();
                String dateTime = invoice.getCreatedDateTime();

                String formattedOnlyYear = formatOnlyYear(date);  // 2025
                String formattedMonthYear = formatMonthYear(date);  // Mar-2025
                String formattedHHmmss = formatHHmmSS(dateTime);  // 13_16_30

                SharedPreferences sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
                String deviceModel = sharedPreferences.getString("model", Build.MODEL);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel+"/"+"invoices");
                databaseReference.child("/"+ formattedOnlyYear +"/"+ formattedMonthYear +"/"+ date +"/"+ formattedHHmmss +"/").removeValue()
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

    private String formatHHmmSS(String dateTime) {

        try {
            if (StringUtils.isNotEmpty(dateTime)) {
                String hms = dateTime.substring(11);

                // Define input date format
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                // Define output format as "MMM-yyyy"
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH_mm_ss", Locale.US);

                // Parse the input date
                Date parsedDate = inputFormat.parse(hms);

                // Format and return the result
                return outputFormat.format(parsedDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid Date";
        }
        return "";
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


}
