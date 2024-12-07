package com.example.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.CustomAdapter;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.utils.PDFGeneratorUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    private static final String SHARED_PREFS_FILE = "my_shared_prefs";

    boolean isLoadFromSystem = true;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private static List<CustomItem> itemList;
    DatabaseHelper dbHelper;
    private ArrayAdapter<String> spinnerAdapter;
    private Map<String,Integer> otherItemsMap;


    ProgressDialog pd;
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private boolean doubleBackToExitPressedOnce = false;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        otherItemsMap = new HashMap<>();
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // For Dark Mode
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // For Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);  // Follow system setting

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkManageStoragePermission();
        } else {
            checkStoragePermission();
        }



        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DatabaseHelper(getApplicationContext());
        pd = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        loadItemsFromSystem(itemList);


        adapter = new CustomAdapter(this, itemList);
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
                    Toast.makeText(MainActivity.this, "Please select 1 item", Toast.LENGTH_SHORT).show();
                }
            }
        });


        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                sendEmail();
//                getAllDocumentsFromCollection("invoices");
                Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
                startActivity(intent);
                return false;
            }
        });

    }

    private void loadItemsFromFile(List<CustomItem> itemList, MenuItem menuItem) {

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "coldrinks.csv");

        List<String> coldDrinks = readCSVFromRawResource(file);
        if(coldDrinks != null && coldDrinks.size()>0){
            menuItem.setTitle("Load Items from System");
            isLoadFromSystem = false;
            itemList.clear();
            coldDrinks.forEach( item -> {
                itemList.add(new CustomItem(item, false, 0));
            });

        } else {
            Toast.makeText(this, "File is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAlerDialogForWritingItems(MenuItem menuItem) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add items");
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.file_items, null, false);

        EditText editText = view.findViewById(R.id.editTextText);
        Button buttonAdd = view.findViewById(R.id.buttonAdd);
        ImageButton buttonClear = view.findViewById(R.id.buttonClear);
        EditText editTextMultiLine = view.findViewById(R.id.editTextTextMultiLine);

        StringBuilder stringBuilder = new StringBuilder();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text =  editText.getText().toString();
                stringBuilder.append(text);
                if(text != null && !text.equals("")){
                    editTextMultiLine.setText(stringBuilder.toString());
                    stringBuilder.append("\n");
                }
                editText.setText(null);
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(null);
                editTextMultiLine.setText(null);
                stringBuilder.setLength(0);
            }
        });

        builder.setView(view);
        builder.setPositiveButton("Write", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveToCsv();
            }

            private void saveToCsv() {
                String text = stringBuilder.toString();
                if (!text.isEmpty()) {
                    try {
                        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "coldrinks.csv");
                        FileWriter writer = new FileWriter(csvFile);
                        writer.append(text);
                        writer.flush();
                        writer.close();
                        Toast.makeText(getApplicationContext(), "Content saved to CSV file", Toast.LENGTH_SHORT).show();
                        loadItemsFromFile(itemList, menuItem);
                        adapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error saving to CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "EditText is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void loadItemsFromSystem(List<CustomItem> itemList) {
        itemList.clear();
        itemList.add(new CustomItem("Orange", false, 0));
        itemList.add(new CustomItem("Kokam", false, 0));
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
        itemList.add(new CustomItem("Btrsch", false, 0));
        itemList.add(new CustomItem("Vanilla", false, 0));
        itemList.add(new CustomItem("Pista", false, 0));
        itemList.add(new CustomItem("Mango", false, 0));
        itemList.add(new CustomItem("Strwbry", false, 0));

    }

    private List<String> readCSVFromRawResource(File file) {
        List<String> coldDrinks = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                coldDrinks.add(line.replace("," ,""));
            }
            br.close();
            return coldDrinks;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "Write storage permission granted", Toast.LENGTH_SHORT).show();
                    // Proceed with your action
                } else {
                    // Permission denied
                    Toast.makeText(this, "Write storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkManageStoragePermission() {
        if (!Environment.isExternalStorageManager()) {
            // Request permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                    Toast.makeText(this, "Manage storage permission granted", Toast.LENGTH_SHORT).show();
                    // Proceed with your action
                } else {
                    // Permission denied
                    Toast.makeText(this, "Manage storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

            CheckBox checkBox = customView.findViewById(R.id.checkbox);
            TextView textView = customView.findViewById(R.id.textView);
            TextView otherTextView = customView.findViewById(R.id.otherTextView);
            LinearLayout dynamicLayout = customView.findViewById(R.id.dynamic_layout);
            textView.setText(null);
            builder.setView(customView);
            builder.setCancelable(false);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
//                        TextView newTextView = new TextView(MainActivity.this);
//                        newTextView.setText("New Dynamic View");
//                        newTextView.setPadding(0, 20, 0, 0);
//                        newTextView.setTextSize(16);

//                        otherEntityView.setVisibility(View.VISIBLE);
                        Map<String, Integer> itemsMap =   showOtherEntityAlert(otherTextView, false, checkBox);
                    } else {
                        otherItemsMap.clear();
                        otherTextView.setText("");
//                        otherEntityView.setVisibility(View.GONE);
//                        dynamicLayout.removeAllViews();
                    }
                }
            });

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

            char bulletSymbol='\u2022';

            itemList = itemList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());
            AtomicInteger ctr = new AtomicInteger(1);
            itemList.stream().forEach(i -> {
                int randomColor = getRandomNiceColor();
                String colorText = (bulletSymbol) + "\t\t " + i.getName() + "\t " + (int) i.getSliderValue() + "\n\n";
                stringBuilder.append(colorText, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            });
            textView.setText(stringBuilder);


            List<CustomItem> finalItemList = itemList;


            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {

                        //map to list;
                        List<CustomItem> otherItemsList = new ArrayList<>();

                        if(otherItemsMap !=null && otherItemsMap.size()>0){
                            otherItemsMap.forEach((k,v) -> {
                                CustomItem customItem = new CustomItem(k,false, 1, v);
                                otherItemsList.add(customItem);
                            });
                        }

                        calculate(finalItemList, otherItemsList);
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

            Boolean otherItemsOnly = true;
            showOtherEntityAlert(null,otherItemsOnly, null);


        }
    }

    private Map<String, Integer> showOtherEntityAlert(TextView otherTextView, Boolean otherItemsOnly, CheckBox checkBox) {

        View view = getLayoutInflater().inflate(R.layout.other_entity, null, false);

        Button btnAddToBucket = view.findViewById(R.id.btnAddToBucket);
        ImageButton btnDelSpinnerItem = view.findViewById(R.id.btnDelSpinnerItem);
        Spinner spinnerBucket = view.findViewById(R.id.spinnerBucket);
        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etItemValue = view.findViewById(R.id.etItemValue);

        List<String> items = new ArrayList<>();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBucket.setAdapter(spinnerAdapter);

        Map<String, Integer> itemMap = new HashMap<>();

//        spinnerBucket.setSelection(0, false);

//        spinnerBucket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String itemName = (String) spinnerBucket.getItemAtPosition(position);
//
//                String itemKey = itemName.split(" = ")[0];
//
//                etItemName.setText(String.valueOf(itemKey));
//                etItemValue.setText(String.valueOf(itemMap.get(itemKey)));
//
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                etItemName.setText("");
//                etItemValue.setText("");
//            }
//        });
        btnAddToBucket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = etItemName.getText().toString();
                String itemValue = etItemValue.getText().toString();
                if(itemName != null && !itemValue.isEmpty() && itemValue != null && !itemValue.isEmpty()){
                    addItemToSpinner(itemName, itemValue);
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
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Other entities");
        builder.setCancelable(false);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Gson gson = new Gson();
                String jsonString = gson.toJson(itemMap);

                System.err.println("MAP json >>> "+itemMap);

                otherItemsMap.putAll(itemMap);

                if(otherItemsOnly){
                    List<CustomItem> otherItemsList = new ArrayList<>();

                    if(otherItemsMap !=null && otherItemsMap.size()>0){
                        otherItemsMap.forEach((k,v) -> {
                            CustomItem customItem = new CustomItem(k,false, 1, v);
                            otherItemsList.add(customItem);
                        });
                        try {
                            calculate(new ArrayList<>(),otherItemsList);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

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
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(otherItemsMap != null && otherItemsMap.size()>0 && otherTextView != null){
                    char bulletSymbol='\u2022';

                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                    stringBuilder.append("Other Items: \n\n");
                    otherItemsMap.forEach((k,v) -> {
                        int randomColor = getRandomNiceColor();
                        String colorText = (bulletSymbol) + "\t\t " + k + "\t " + v + "\n\n";
                        stringBuilder.append(colorText, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    });
                    otherTextView.setText(stringBuilder);
                } else {
                    if(checkBox != null){
                        checkBox.setChecked(false);
                    }
                }
            }
        });

        dialog.show();
        return itemMap;
    }

    private int getRandomNiceColor() {
        // Define an array of specific nice-looking colors
        int[] colors = new int[] {
                Color.rgb(129, 199, 132),  // Light Green
                Color.rgb(100, 181, 246),  // Light Blue
                Color.rgb(255, 213, 79),   // Amber
                Color.rgb(239, 83, 80),    // Light Red
                Color.rgb(186, 104, 200),  // Light Purple
                Color.rgb(255, 167, 38),   // Orange
                Color.rgb(38, 198, 218),   // Cyan
                Color.rgb(255, 112, 67),   // Deep Orange
                Color.rgb(255, 241, 118),  // Light Yellow
                Color.rgb(174, 213, 129)   // Pale Green
        };

        // Generate a random index to pick a color
        Random random = new Random();
        int randomIndex = random.nextInt(colors.length);

        // Return the randomly selected color
        return colors[randomIndex];
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculate(List<CustomItem> itemList, List<CustomItem> otherItemsList) throws IOException {
        itemList = itemList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());
        otherItemsList = otherItemsList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());

        if(itemList.size()>0 || otherItemsList.size()>0){

            Long grandTotal = getTotal(itemList,otherItemsList);

            DtoJson dtoJson = new DtoJson();
            dtoJson.setName(null);
            dtoJson.setDate(String.valueOf(LocalDate.now()));
            dtoJson.setCreateddtm(String.valueOf(LocalDateTime.now()));
            dtoJson.setTotal(grandTotal);
            dtoJson.setItemList(itemList);
            dtoJson.setOtherItemsList(otherItemsList);

            String dtoJsonStr =  convertCustomItemsToJson(dtoJson);
            System.err.println(dtoJsonStr);

            long newRowId = dbHelper.saveInvoiceTransaction(dtoJsonStr,grandTotal, MainActivity.this);
            if(newRowId != -1){
                writeTextFile(dtoJsonStr);
                File pdfFile = PDFGeneratorUtil.generateInvoice(dtoJson, newRowId, getApplicationContext());
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, "Invoice generated "+newRowId, Snackbar.LENGTH_LONG)
                        .setDuration(5000)
                        .setAction("View", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openGeneratedPDF(pdfFile);
                                // Create an Intent to open the generated invoice
                            }
                        })
                        .show();
                writeAllDbContentInTxtFile(dtoJson,dbHelper);
                resetSliders();
            }
        } else {
            Toast.makeText(this, "Please select minimum 1 item", Toast.LENGTH_SHORT).show();
        }

//        PDFGenerator.generateInvoicePDF(MainActivity.this, dtoJsonStr);
//        writeTextFile(dtoJsonStr);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateInvoice(DtoJson dtoJson, long newRowId) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

            String pdfPath = pdfPathMain+File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator+"invoices";

            if(!Files.exists(Paths.get(pdfPath))){
                new File(pdfPath).mkdirs();
            }

            LocalDateTime localDateTime = LocalDateTime.now();
            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            File pdfFile = new File(pdfPath, "invoice_"+frmtted+".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Load the image from assets
            AssetManager assetManager = getApplicationContext().getAssets();
            InputStream inputStream = assetManager.open("invoice (4).png");
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);
            image.setWidth(30);
            image.setHeight(30);
            // Add the image to the document
            document.add(image);


            Div div1 = new Div();
            div1.setMargins(10,5,10,5);


            float[] columnWidths = {1, 15};
            Table tableTe = new Table(columnWidths);
            tableTe.setBorder(Border.NO_BORDER);

            // Add title
            Paragraph headingTitle = new Paragraph("Gajanan Coldrinks House")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(20)
                    .setBold();
            Paragraph title = new Paragraph("Invoice " + newRowId)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(20)
                    .setBold();


            // Add cells with words
            tableTe.addCell(new Cell().setWidth((pdfDocument.getDefaultPageSize().getWidth()/2)).setBorder(Border.NO_BORDER).add(headingTitle));
            tableTe.addCell(new Cell().setWidth((pdfDocument.getDefaultPageSize().getWidth()/2)).setBorder(Border.NO_BORDER).add(title));

            // Add the table to the document
            document.add(tableTe);
            document.add(div1);

//            div1.add(headingTitle);
//            div2.add(title);

//            div1.setMarginRight(10);
//            div2.setMarginLeft(columnWidth + 10);

//            document.add(div2);


            String name = dtoJson.getName();

            if(name == null){
                name = "";
            }

            String date = dtoJson.getCreateddtm();
            String dateFrmtted = getFormattedDateTime(date);

            // Add customer details
            Paragraph customerDetails = new Paragraph()
                    .add("Name: " + name + "\n")
                    .add("Date & Time: " + dateFrmtted + "\n\n");
            document.add(customerDetails);

            // Add table for items
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
                    .useAllAvailableWidth();
            table.setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(new Cell().setBold().add(new Paragraph("Item Description")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().setBold().add(new Paragraph("Rate")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().setBold().add(new Paragraph("Quantity")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().setBold().add(new Paragraph("Amount")).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            List<CustomItem> itemList = dtoJson.getItemList();
            List<CustomItem> otherItemsList = dtoJson.getOtherItemsList();
            itemList.addAll(otherItemsList);
            if (itemList != null) {
                for (CustomItem item : itemList) {
                    table.addCell(new Cell().add(new Paragraph(item.getName())));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(sharedPreferences.getInt(item.getName().toUpperCase(Locale.getDefault()),0)))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf((int) item.getSliderValue()))));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getAmount()))));
                }
            }

            document.add(table);

            // Add total
            Paragraph total = new Paragraph("Total: " + dtoJson.getTotal())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(15)
                    .setBold();
            document.add(total);

            document.close();
            writer.close();

            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, "Invoice generated "+newRowId, Snackbar.LENGTH_LONG)
                    .setDuration(5000)
                    .setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openGeneratedPDF(pdfFile);
                            // Create an Intent to open the generated invoice
                        }
                    })
                    .show();





        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFormattedDateTime(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
        String formattedDate = dateTime.format(outputFormatter);
        return formattedDate;
    }

    private void openGeneratedPDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Open PDF");
        try {
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            // Handle the case where no PDF reader is installed
            Toast.makeText(this, "No application found to open PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetSliders() {
        otherItemsMap.clear();
        itemList = itemList.stream().map(i -> {
            i.setSliderValue(0);
            return i;
        }).collect(Collectors.toList());
        adapter.notifyDataSetChanged();
    }

    private void writeAllDbContentInTxtFile(DtoJson dtoJson, DatabaseHelper dbHelper) throws IOException {
        dbHelper.getAllDbRecords(dtoJson,MainActivity.this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeTextFile(String dtoJsonStr) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        String subPath = path + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "json data";

        if(!Files.exists(Paths.get(subPath))){
            new File(subPath).mkdirs();
        }

        String filePath = subPath + File.separator + "Invoice_Dtl_Jsn_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".txt";
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

    private Long getTotal(List<CustomItem> itemList, List<CustomItem> otherItemsList) {
         long items = (long) itemList.stream().mapToDouble(CustomItem::getAmount).sum();
         long otherItems = (long) otherItemsList.stream().mapToDouble(CustomItem::getAmount).sum();
         return items + otherItems;
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
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 700); // Time window for double press in milliseconds (2 seconds)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_backup_via) {
            sendEmailWithAttachment();
            return true;
        }

        if (id == R.id.action_delete_cloud_date) {
            deleteEntireCloudData();
            return true;
        }

        if (id == R.id.action_test) {
            callTest();
            return true;
        }

        if (id == R.id.action_reports) {
            callReport();
            return true;
        }


        if (id == R.id.action_add_item) {
            addMenuItem();
            return true;
        }


//        if (id == R.id.action_load_from_file) {
//            if(!isLoadFromSystem){
//                loadItemsFromSystem(itemList);
//                adapter.notifyDataSetChanged();
//                isLoadFromSystem = true;
//                item.setTitle("Load items from File");
//            } else {
//                reloadMainAcitivyItems(item);
//            }
//            return true;
//        }

        // Handle other action bar items...

        return super.onOptionsItemSelected(item);
    }


    private void addMenuItem() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Add menu item");
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.add_menu_item, null, false);

        EditText etAddItem = view.findViewById(R.id.addItemEditText);
        EditText etItemPrice = view.findViewById(R.id.etItemPrice);

        etAddItem.requestFocus();

        builder.setView(view);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(etAddItem != null && !etAddItem.getText().toString().isEmpty() &&
                        etItemPrice != null && !etItemPrice.getText().toString().isEmpty()) {
                    String itemName = etAddItem.getText().toString();
                    String itemPrice = etItemPrice.getText().toString();
                    updateMainItemList(itemName, itemPrice);
                    Toast.makeText(MainActivity.this, "Item added "+itemName + "  (Rs. "+itemPrice+")", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void updateMainItemList(String itemName, String itemPrice) {
        CustomItem item = new CustomItem(itemName, false, 0.0f, Integer.parseInt(itemPrice));
        itemList.add(item);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        // Use the editor to put values into SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(itemName.toUpperCase(Locale.getDefault()), Integer.parseInt(itemPrice));
        // Commit the changes
        editor.apply();


        adapter.notifyDataSetChanged();
    }


    private void reloadMainAcitivyItems(MenuItem item) {
//        openAlerDialogForWritingItems(item);
        loadItemsFromFile(itemList,item);
        adapter.notifyDataSetChanged();
    }

    private void callReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    private void callTest() {
        Intent intent = new Intent(this, SharedPrefActivity.class);
        intent.putExtra("sharedPrefList", (Serializable) itemList);
        startActivity(intent);
    }

    private void deleteEntireCloudData() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);

        builder.setTitle("Are you sure you want to delete all the cloud data?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.setMessage("Please wait");
                pd.show();
                dbHelper.deleteFireStoreData(MainActivity.this,pd);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendEmailWithAttachment() {

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "InvoicesBackup.csv";

        File file = new File(filePath);

        Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/csv");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, InvoiceConstants.EMAIL_RECIPIENTS);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoices Backup "+ new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date()));
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email body text");


        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (ActivityNotFoundException ex) {
            Log.e("MainActivity", "No email client found", ex);
        }


    }


}