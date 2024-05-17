package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private static List<CustomItem> itemList;
    DatabaseHelper dbHelper;

    ProgressDialog pd;

    
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DatabaseHelper(getApplicationContext());
        pd = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        itemList.add(new CustomItem("Kokam", false, 0));
        itemList.add(new CustomItem("Orange", false, 0));
        itemList.add(new CustomItem("L. Lemon", false, 0));
        itemList.add(new CustomItem("L. Orange", false, 0));
        itemList.add(new CustomItem("Sarbat", false, 0));
        itemList.add(new CustomItem("S Sarbat", false, 0));
        itemList.add(new CustomItem("Pachak", false, 0));
        itemList.add(new CustomItem("L. Soda", false, 0));
        itemList.add(new CustomItem("Wala", false, 0));
        itemList.add(new CustomItem("Lassi_H", false, 0));
        itemList.add(new CustomItem("Lassi_F", false, 0));
        itemList.add(new CustomItem("J. Soda", false, 0));
        itemList.add(new CustomItem("Taak", false, 0));
        itemList.add(new CustomItem("Kulfi", false, 0));
        itemList.add(new CustomItem("Stwbry Soda", false, 0));
        itemList.add(new CustomItem("Water_H", false, 0));
        itemList.add(new CustomItem("Water_F", false, 0));
        itemList.add(new CustomItem("Mng_Lssi_H", false, 0));
        itemList.add(new CustomItem("Mng_Lssi_F", false, 0));
        itemList.add(new CustomItem("Taak", false, 0));
        itemList.add(new CustomItem("Btrsch", false, 0));

        adapter = new CustomAdapter(itemList);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        recyclerView.setItemViewCacheSize(itemList.size());
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(itemList.size()>0){
                    showAlertDialog(itemList);
                } else {
                    Toast.makeText(MainActivity.this, "Plz select 1 item", Toast.LENGTH_SHORT).show();
                }
            }
        });


        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                sendEmail();
                getAllDocumentsFromCollection("invoices");
                return false;
            }
        });

    }

    private void getSHA() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("MY KEY HASH:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }


    private void getAllDocumentsFromCollection(String invoices) {
        pd.setMessage("Loading please wait");
        pd.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection(invoices);

        Query query = collectionRef.orderBy("invoice_id", Query.Direction.DESCENDING);

        // Fetch all documents in the collection
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        pd.dismiss();
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        // Process the documents

                        StringBuilder stringBuilder = new StringBuilder();


                        for (DocumentSnapshot document : documents) {
//                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Long id = document.getLong("invoice_id");
                            Long total =document.getLong("total");
                            String createdDate = document.getString("created_date");
                            String createdDateTime = document.getString("created_date_time");
                            String jsonList = document.getString("item_list_json");

                            stringBuilder.append(id+" "+jsonList+" "+total+" "+createdDateTime+" "+createdDate+"\n\n\n\n|=================================|\n\n\n\n");

                            // You can access individual fields like this:
                            // String field1 = document.getString("field1");
                            // int field2 = document.getLong("field2").intValue();
                            // etc.
                        }
                        showAlert(stringBuilder);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(MainActivity.this, "Error getting documents from collection "+invoices, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAlert(StringBuilder stringBuildercontent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("All Cloud data");
        final View customView = getLayoutInflater().inflate(R.layout.custom_table_collection, null);
        TextView textView = customView.findViewById(R.id.textView);
        textView.setText(null);
        builder.setView(customView);
        builder.setCancelable(false);
        if(stringBuildercontent != null){
            textView.setText(stringBuildercontent.toString());
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAlertDialog(List<CustomItem> itemList) {
        itemList = itemList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());

        if(itemList.size()>0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Review items \t(Total "+itemList.stream().mapToInt(CustomItem::getAmount).sum()+")");
            final View customView = getLayoutInflater().inflate(R.layout.custom_table, null);
            TextView textView = customView.findViewById(R.id.textView);
            textView.setText(null);
            builder.setView(customView);
            builder.setCancelable(false);
            StringBuilder stringBuilder = new StringBuilder();
            itemList = itemList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());
            AtomicInteger ctr = new AtomicInteger(1);
            itemList.stream().forEach(i -> {
                stringBuilder.append((ctr.getAndIncrement()) + "\t " + i.getName() + "\t " + (int) i.getSliderValue() + "\n\n");
            });
            textView.setText(stringBuilder.toString());


            List<CustomItem> finalItemList = itemList;
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        calculate(finalItemList);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "Please select minimum 1 item", Toast.LENGTH_SHORT).show();
        }
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculate(List<CustomItem> itemList) throws IOException {
        itemList = itemList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());

        if(itemList.size()>0){

            Long grandTotal = getTotal(itemList);

            DtoJson dtoJson = new DtoJson();
            dtoJson.setName(null);
            dtoJson.setDate(String.valueOf(LocalDate.now()));
            dtoJson.setCreateddtm(String.valueOf(LocalDateTime.now()));
            dtoJson.setTotal(grandTotal);
            dtoJson.setItemList(itemList);

            String dtoJsonStr =  convertCustomItemsToJson(dtoJson);
            System.err.println(dtoJsonStr);

            long newRowId = dbHelper.saveInvoiceTransaction(dtoJsonStr,grandTotal, MainActivity.this);
            if(newRowId != -1){
//                writeTextFile(dtoJsonStr);
                resetSliders();
            }
        } else {
            Toast.makeText(this, "Please select minimum 1 item", Toast.LENGTH_SHORT).show();
        }

//        PDFGenerator.generateInvoicePDF(MainActivity.this, dtoJsonStr);
//        writeTextFile(dtoJsonStr);

//        writeAllDbContentInTxtFile(dbHelper);
//        putDataFireStore(null,null);
    }

    private void resetSliders() {
        itemList = itemList.stream().map(i -> {
            i.setSliderValue(0);
            return i;
        }).collect(Collectors.toList());
        adapter.notifyDataSetChanged();
    }

    private void writeAllDbContentInTxtFile(DatabaseHelper dbHelper) throws IOException {
        dbHelper.getAllDbRecords(MainActivity.this);
    }

    private void writeTextFile(String dtoJsonStr) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path+File.separator+"Invoice_Detailed_Json.txt";
        try {
            // Create a BufferedWriter to write to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            // Write the content to the file
            writer.write(dtoJsonStr);
            // Close the writer
            writer.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error writing to the file: ",Toast.LENGTH_SHORT).show();
        }
    }

    private Long getTotal(List<CustomItem> itemList) {
        return (long) itemList.stream().mapToDouble(CustomItem::getAmount).sum();
    }


    private String convertCustomItemsToJson(DtoJson dtoJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(dtoJson);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Toast.makeText(this, "Cool", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Handle other action bar items...

        return super.onOptionsItemSelected(item);
    }


}