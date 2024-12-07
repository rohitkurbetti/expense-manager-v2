package com.example.myapplication.database;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.myapplication.dtos.DtoJson;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invoices.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    public long saveInvoiceTransaction(String itemListJson,Long grandTotal, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total", grandTotal);
        values.put("item_list_json", itemListJson);
        values.put("created_date_time", String.valueOf(new Timestamp(System.currentTimeMillis())));
        values.put("created_date", String.valueOf(LocalDate.now()));

        long newRowId = db.insert("invoices", null, values);
        if (newRowId != -1) {
//            Toast.makeText(context, "Invoice inserted in database", Toast.LENGTH_SHORT).show();
            putDataFireStore(context, values, newRowId);
        } else {
            Toast.makeText(context, "Invoice insertion failed", Toast.LENGTH_LONG).show();
        }
        return newRowId;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void putDataFireStore(Context context, ContentValues values, long newRowId) {
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Create a new user document with a generated ID

        DocumentReference newUserRef = db.collection("invoices").document(LocalDate.now()+"_"+newRowId);

// Create a Map to represent the data
        Map<String, Object> user = new HashMap<>();
        user.put("invoice_id" , newRowId);
        user.put("item_list_json" , values.get("item_list_json"));
        user.put("total", values.get("total"));
        user.put("created_date_time", values.get("created_date_time"));
        user.put("created_date", values.get("created_date"));

// Add the data to Firestore
        newUserRef.set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Invoice uploaded on cloud: "+newRowId, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Invoice upload failed!", Toast.LENGTH_SHORT).show();
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
}
