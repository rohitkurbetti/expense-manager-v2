package com.example.myapplication;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
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
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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

import com.example.myapplication.adapters.CsvAdapter;
import com.example.myapplication.adapters.CustomAdapter;
import com.example.myapplication.adapters.NestedListAdapter;
import com.example.myapplication.adapters.NestedOtherListAdapter;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.utils.PDFGeneratorUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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


import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {

    private static final String SHARED_PREFS_FILE = "my_shared_prefs";
    private static final String FIRST_LAUNCH_KEY = "isFirstLaunch";

    private final String csvFileName = "item_list.csv";
    private boolean isIconOne = true;
    boolean isLoadFromSystem = true;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    public static List<CustomItem> itemList;
    DatabaseHelper dbHelper;
    private ArrayAdapter<String> spinnerAdapter;
    private Map<String,Integer> otherItemsMap;
    ProgressDialog pd;
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private boolean doubleBackToExitPressedOnce = false;
    private List<CustomItem> otherItemsList;
    private ListView otherListView;
    private TextView bannerOtherItems;
    private TextView selectDateLink;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);

        // Check if app is launched for the first time
        if (isFirstLaunch()) {
            showSetItemPricesDialog();
        }



        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DatabaseHelper(getApplicationContext());
        pd = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerView);
        TextView tvSelectiveTotalText = findViewById(R.id.tvSelectiveTotalText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();
        loadItemsFromSystem(itemList);


        adapter = new CustomAdapter(this, itemList, tvSelectiveTotalText);
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

    private boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    private void showSetItemPricesDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Set Item Prices")
                .setMessage("Please set the item prices")
                .setPositiveButton("Set", (dialog, which) -> {
                    // Redirect to Set Item Prices Activity
                    callTest();

                    // Save flag to prevent dialog from showing again
//                    markFirstLaunchDone();
                })
                .setNegativeButton("Don't set", (dialog, which) -> {
                    // Just dismiss the dialog
//                    markFirstLaunchDone();
                    dialog.dismiss();
                })
                .setCancelable(false) // Prevent dismissal by tapping outside
                .show();
    }

    private void markFirstLaunchDone() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_LAUNCH_KEY, false);
        editor.apply();
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
        if(loadItemsFromInteralStorage(itemList, csvFileName)) {
//            List<CustomItem> itemList1 = readCSVFromAssets(this, csvFileName);
//            itemList.addAll(itemList1);
//            itemList1.clear();



//            appendToCSVInDownloads(fileName,"Fruit Beer");

//            loadItemsFromInteralStorage(itemList, csvFileName);


        } else {

            itemList.add(new CustomItem("Orange", false, 0));
            itemList.add(new CustomItem("Lemon", false, 0));
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



    }

    private boolean loadItemsFromInteralStorage(List<CustomItem> itemList, String fileName) {
        itemList.clear();
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDir, fileName);

        if (!csvFile.exists()) {
            Log.e("CSVReader", "CSV file does not exist in Downloads folder");
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming CSV contains only one column per line: itemName
                itemList.add(new CustomItem(line.trim(), false, 0));
            }
            reader.close();
        } catch (IOException e) {
            Log.e("CSVReader", "Error reading CSV file", e);
            return false;
        }
        if(itemList.size() == 0) {
            return false;
        }
        return true;
    }

    private void appendToCSVInDownloads(String fileName, String itemName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDir, fileName);

        try {
            FileWriter fileWriter = new FileWriter(csvFile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(itemName);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e("CSVReader", "Error writing to CSV file", e);
        }
    }

    private void saveCSVInDownloads(String fileName, List<CustomItem> items) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File csvFile = new File(downloadsDir, fileName);

        try {
            FileWriter fileWriter = new FileWriter(csvFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (CustomItem csvItem : items) {
                bufferedWriter.write(csvItem.getName());
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            Toast.makeText(this, "Inserted "+ items.size() +" items", Toast.LENGTH_SHORT).show();
//            loadItemsFromInteralStorage(items, fileName);
        } catch (IOException e) {
            Log.e("CSVReader", "Error writing to CSV file", e);
        }
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showAlertDialog(List<CustomItem> itemList) {
        itemList = itemList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());

        if(itemList.size()>0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Review items");
            builder.setIcon(R.drawable.checklist);
//            builder.setTitle("Review items \t(Total "+itemList.stream().mapToInt(CustomItem::getAmount).sum()+")");
            final View customView = getLayoutInflater().inflate(R.layout.custom_table, null);

            CheckBox checkBox = customView.findViewById(R.id.checkbox);
            TextView textView = customView.findViewById(R.id.textView);
            ListView nestedListView = customView.findViewById(R.id.nestedListView);
            otherListView = customView.findViewById(R.id.otherListView);
            bannerOtherItems = customView.findViewById(R.id.bannerOtherItems);
            selectDateLink = customView.findViewById(R.id.selectDateLink);
//            TextView otherTextView = customView.findViewById(R.id.otherTextView);
            LinearLayout dynamicLayout = customView.findViewById(R.id.dynamic_layout);
            textView.setText(null);
            builder.setCancelable(false);


            // Set the ListView adapter
            NestedListAdapter nestedListAdapter = new NestedListAdapter(this, itemList, nestedListView);
            nestedListView.setAdapter(nestedListAdapter);
            nestedListAdapter.notifyDataSetChanged();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

            LocalDate localDate = LocalDate.now();

            String formtted = localDate.format(dateTimeFormatter);

            selectDateLink.setText(formtted);

            selectDateLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the current date
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    // Show DatePickerDialog
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            MainActivity.this,
                            (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                                // Handle selected date (month is 0-indexed, so add 1)
                                String monthName = Month.of(selectedMonth+1).getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH);
                                String selectedDate = selectedDay + "-" + monthName + "-" + selectedYear;
                                String strFmtDay = String.format("%02d",selectedDay);
                                selectedDate = strFmtDay + "-" + monthName + "-" + selectedYear;
                                selectDateLink.setText(selectedDate);
                            },
                            year, month, day);

                    datePickerDialog.show();
                }
            });





            builder.setView(customView);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
//                        TextView newTextView = new TextView(MainActivity.this);
//                        newTextView.setText("New Dynamic View");
//                        newTextView.setPadding(0, 20, 0, 0);
//                        newTextView.setTextSize(16);

//                        otherEntityView.setVisibility(View.VISIBLE);
                        Map<String, Integer> itemsMap =   showOtherEntityAlert(null, false, checkBox);
                    } else {
                        otherItemsMap.clear();
//                        otherTextView.setText("");
//                        otherEntityView.setVisibility(View.GONE);
//                        dynamicLayout.removeAllViews();
                    }
                }
            });

//            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

//            char bulletSymbol='\u2022';
//
//            itemList = itemList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());
//            AtomicInteger ctr = new AtomicInteger(1);
//            itemList.stream().forEach(i -> {
//                int randomColor = getRandomNiceColor();
//                String colorText = (bulletSymbol) + "\t\t " + i.getName() + "\t " + (int) i.getSliderValue() + "\n\n";
//                stringBuilder.append(colorText, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            });
//            textView.setText(stringBuilder);


            List<CustomItem> finalItemList = itemList;


            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {

                        //map to list;
                        otherItemsList = new ArrayList<>();

                        if(otherItemsMap !=null && otherItemsMap.size()>0){
                            otherItemsMap.forEach((k,v) -> {
                                CustomItem customItem = new CustomItem(k,false, 1, v);
                                otherItemsList.add(customItem);
                            });
                        }

                        calculate(finalItemList, otherItemsList, selectDateLink.getText().toString(), false);
                        otherItemsList.clear();
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
        ImageView btnDelSpinnerItem = view.findViewById(R.id.btnDelSpinnerItem);
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
                    showCustomToast("Item " + itemName + " added");
                    animateBackground(spinnerBucket , false);

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
        builder.setTitle("Other entities");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.checklist);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Gson gson = new Gson();
                String jsonString = gson.toJson(itemMap);

                System.err.println("MAP json >>> "+itemMap);

                otherItemsMap.putAll(itemMap);
                otherItemsList = new ArrayList<>();
//                if(otherItemsOnly){


                    if(otherItemsMap !=null && otherItemsMap.size()>0){
                        otherItemsMap.forEach((k,v) -> {
                            CustomItem customItem = new CustomItem(k,false, 1, v);
                            otherItemsList.add(customItem);
                            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                            // Use the editor to put values into SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(k.toUpperCase(Locale.getDefault()), v);
                            // Commit the changes
                            editor.apply();
                        });
                        try {
                            if(otherItemsOnly){
                                calculate(new ArrayList<>(),otherItemsList, null, otherItemsOnly);
                                otherItemsList.clear();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
//                }

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
                if(otherItemsList != null && otherItemsList.size()>0 ){
//                    char bulletSymbol='\u2022';
//
//                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
//                    stringBuilder.append("Other Items: \n\n");
//                    otherItemsMap.forEach((k,v) -> {
//                        int randomColor = getRandomNiceColor();
//                        String colorText = (bulletSymbol) + "\t\t " + k + "\t " + v + "\n\n";
//                        stringBuilder.append(colorText, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                    });
//                    otherTextView.setText(stringBuilder);
                    otherListView.setVisibility(View.VISIBLE);
                    bannerOtherItems.setVisibility(View.VISIBLE);

                    NestedOtherListAdapter nestedOtherListAdapter = new NestedOtherListAdapter(builder.getContext(), otherItemsList,otherListView, checkBox,bannerOtherItems);
                    otherListView.setAdapter(nestedOtherListAdapter);

                    nestedOtherListAdapter.notifyDataSetChanged();

                } else {
//                    otherListView.setVisibility(View.GONE);
//                    bannerOtherItems.setVisibility(View.GONE);
                    if(checkBox != null){
                        checkBox.setChecked(false);
                    }
                }
            }
        });

        dialog.show();
        return itemMap;
    }

    private void animateBackground(Spinner spinnerBucket, boolean isDelete) {
        // Define the start and end colors.
        // Start with current background color (assume transparent if not set)
        int colorFrom = Color.WHITE;
        int colorTo = Color.parseColor("#ffd966"); // Light yellow

        // Determine whether the current theme is dark or light
        int currentNightMode = spinnerBucket.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Dark theme: Use a subtle dark highlight (e.g., a muted gray or dark accent)
            colorTo = Color.parseColor("#555555"); // Example: dark gray highlight for dark theme

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.colorBackgroundFloating, typedValue, true);
            int alertDialogBgColor = typedValue.data;

            colorFrom = alertDialogBgColor;
        } else {
            // Light theme: Use a light yellow highlight
            colorTo = Color.parseColor("#ffd966"); // Light yellow
            colorFrom = Color.WHITE;
        }
//        if(isDelete) {
//            colorTo = Color.parseColor("#FF9999");
//        }


        // Create a ValueAnimator that goes from the start color to the end color and back to the start color.
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo, colorFrom);
        colorAnimation.setDuration(900); // Total animation duration in milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                spinnerBucket.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private void setListViewHeightBasedOnChildren(View listView, ViewGroup parent) {
        ListAdapter listAdapter = this.otherListView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, parent);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = this.otherListView.getLayoutParams();
        params.height = totalHeight + (this.otherListView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
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
    private void calculate(List<CustomItem> itemList, List<CustomItem> otherItemsList, String selectDate, Boolean otherItemsOnly) throws IOException {
        itemList = itemList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());
        otherItemsList = otherItemsList.stream().filter(i -> i.getSliderValue()>0.0f).collect(Collectors.toList());

        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMM-yyyy").toFormatter(Locale.ENGLISH);
        if(selectDate==null){
            selectDate = String.valueOf(LocalDate.now());
        }
        LocalDate localDate=LocalDate.now();
        if(otherItemsOnly) {
            localDate = LocalDate.parse(selectDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            localDate = LocalDate.parse(selectDate, dateTimeFormatter);
        }
        if(itemList.size()>0 || otherItemsList.size()>0){

            Long grandTotal = getTotal(itemList,otherItemsList);

            DtoJson dtoJson = new DtoJson();
            dtoJson.setName(null);
            dtoJson.setDate(localDate.toString());
            dtoJson.setCreateddtm(createDtmFromLocalDate(localDate));
            dtoJson.setTotal(grandTotal);
            dtoJson.setItemList(itemList);
            dtoJson.setOtherItemsList(otherItemsList);

            String dtoJsonStr =  convertCustomItemsToJson(dtoJson);
            System.err.println(dtoJsonStr);

            long newRowId = dbHelper.saveInvoiceTransaction("\""+dtoJsonStr+"\"",grandTotal, MainActivity.this, dtoJson, null);
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
    private String createDtmFromLocalDate(LocalDate localDate) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDate.toString()+" "+String.format("%02d",localDateTime.getHour())+":"
                +String.format("%02d",localDateTime.getMinute())+":"+String.format("%02d",localDateTime.getSecond());
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
        Gson gson = new GsonBuilder().create();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();

        if (id == R.id.action_expense_manager) {
            launchExpenseManager();
            return true;
        }

        if (id == R.id.action_backup_via) {
            sendEmailWithAttachment();
            return true;
        }

        if (id == R.id.action_restore) {
            try {
                restoreFromBackedUpCsv();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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


//        if (id == R.id.action_add_item) {
//            addMenuItem();
//            return true;
//        }

        if (id == R.id.action_delete_item) {
            deleteItem();
            return true;
        }

        if (id == R.id.action_fetch_cloud_data) {
            getAllCloudDetails();
            return true;
        }

        if (id == R.id.resetItemCsv) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Code to execute after 500ms
                resetItemCsv();
            }, 500);

            return true;
        }

        if (id == R.id.deleteSqliteData) {
            dbHelper.deleteAllData();
            Toast.makeText(this, "All app data deleted", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.toggle_cloud_store) {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
            // Use the editor to put values into SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("toggleCloudStore", !sharedPreferences.getBoolean("toggleCloudStore", false));
            editor.apply();

            String cloudStorageState = "Off";
            if(sharedPreferences.getBoolean("toggleCloudStore", false)) {
                cloudStorageState = "On";
                item.setIcon(R.drawable.cloud_computing_enabled_data);  // Switch to icon two
            } else {
                cloudStorageState = "Off";
                item.setIcon(R.drawable.cloud_computing_nodata);  // Switch back to icon one
            }
            isIconOne = !isIconOne;

            Toast.makeText(this, "Cloud storage: "+cloudStorageState, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.config) {
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.sqliteData) {
            Intent intent = new Intent(this, SqliteDataActivity.class);
            startActivity(intent);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void restoreFromBackedUpCsv() throws IOException {

        List<DtoJsonEntity> invoices = new ArrayList<>();

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "InvoicesBackup.csv";

        File docsDir = new File(filePath);

        if (!docsDir.exists()) {
            Log.e("CSVReaderUtil", "File not found: " + docsDir.getAbsolutePath());
        }


        // Read CSV file
        BufferedReader reader = new BufferedReader(new FileReader(docsDir));
        String line;
        boolean firstLine = true;
        int counter =0;
        while ((line = reader.readLine()) != null) {
//            if (firstLine) {
//                firstLine = false; // Skip header line
//                continue;
//            }

            // Split CSV by commas, handling JSON strings with quotes
            String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

            if (columns.length >= 5) {
                Long invoiceId = Long.parseLong(columns[0].trim());
                String itemListJson = columns[1].trim();
                Long total = Long.parseLong(columns[2].trim());
                String createdDateTime = columns[3].trim();
                String createdDate = columns[4].trim();

                DtoJson dtoJson = new DtoJson();
                dtoJson.setCreateddtm(createdDateTime);
                dtoJson.setDate(createdDate);

                long newRowId = dbHelper.saveInvoiceTransaction(itemListJson,total, MainActivity.this, dtoJson, invoiceId);
                if(newRowId != -1) {
                    ++counter;
                }
            }
        }
        Toast.makeText(this, "Restored "+counter+" rows", Toast.LENGTH_SHORT).show();
        reader.close();



    }

    private void launchExpenseManager() {
        Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
        startActivity(intent);
    }

    private void deleteItem() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add / Remove items");
        builder.setCancelable(false);
        builder.setIcon(getDrawable(R.drawable.checklist));
        List<CustomItem> csvItemsList = new ArrayList<>();
        loadItemsFromInteralStorage(csvItemsList, csvFileName);

        View csvViewLayout = getLayoutInflater().inflate(R.layout.csv_item_list, null , false);
        ListView csvListView = csvViewLayout.findViewById(R.id.csvListView);
        EditText etCsvItemName = csvViewLayout.findViewById(R.id.etCsvItemName);
        Button btnAddCsvItem = csvViewLayout.findViewById(R.id.btnAddCsvItem);

        etCsvItemName.requestFocus();

        CsvAdapter csvAdapter= new CsvAdapter(this, csvItemsList);
        csvListView.setAdapter(csvAdapter);

        btnAddCsvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etCsvItemName!=null && StringUtils.isNotEmpty(etCsvItemName.getText())
                        && !StringUtils.isNumeric(etCsvItemName.getText())) {
                    String csvItemName = etCsvItemName.getText().toString();
                    csvItemsList.add(new CustomItem(csvItemName, false, 0));
                    csvAdapter.notifyDataSetChanged();
                    etCsvItemName.setText("");
                    csvListView.post(() -> csvListView.setSelection(csvAdapter.getCount() - 1));

                }
            }
        });




        builder.setView(csvViewLayout);


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveCSVInDownloads(csvFileName, csvItemsList);
                itemList.clear();
                itemList.addAll(csvItemsList);
                adapter.notifyDataSetChanged();
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

    private List<CustomItem> readCSVFile(String fileName) {
        List<CustomItem> items = new ArrayList<>();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        if (!file.exists()) {
            Log.e("TAG", "CSV file not found: " + file.getAbsolutePath());
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Assuming CSV contains only one column per line: itemName
                items.add(new CustomItem(line.trim(), false, 0));
            }

        } catch (IOException | NumberFormatException e) {
            Log.e("TAG ", "Error reading CSV file", e);
        }

        return items;
    }

    private void resetItemCsv() {

        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;

        try {
            // Open the CSV file from assets
            in = assetManager.open(csvFileName);

            // Define the Downloads directory path
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Create the destination file
            File outFile = new File(downloadsDir, csvFileName);
            out = new FileOutputStream(outFile);

            // Copy the file contents from assets to the Downloads directory
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            // Close the streams
            in.close();
            out.close();
            Toast.makeText(this, "Csv reset done", Toast.LENGTH_SHORT).show();
            loadItemsFromInteralStorage(itemList, csvFileName);

            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            Toast.makeText(this, "Error :"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private void createThatProgressMenu() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    private void getAllCloudDetails() {
        Intent intent = new Intent(this, CloudSummary.class);
        startActivity(intent);
    }


    private void addMenuItem() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Add menu item");
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.add_menu_item, null, false);

        EditText etAddItem = view.findViewById(R.id.addItemEditText);
//        EditText etItemPrice = view.findViewById(R.id.etItemPrice);

        etAddItem.requestFocus();

        builder.setView(view);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(etAddItem != null && !etAddItem.getText().toString().isEmpty()
//                        && etItemPrice != null && !etItemPrice.getText().toString().isEmpty()
                ) {
                    String itemName = etAddItem.getText().toString();
//                    String itemPrice = etItemPrice.getText().toString();
//                    updateMainItemList(itemName, itemPrice);
//                    Toast.makeText(MainActivity.this, "Item added "+itemName + "  (Rs. "+itemPrice+")", Toast.LENGTH_SHORT).show();


                    appendToCSVIfNotExists(itemName);


                }

            }

            private void appendToCSVIfNotExists(String itemName) {
                if (!isItemInCSV(itemName)) {
                    appendToCSVInDownloads(csvFileName, itemName);
                    loadItemsFromInteralStorage(itemList, csvFileName);
                    Toast.makeText(MainActivity.this, "Item appended: " + itemName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("CSVReader", "Item already exists: " + itemName);
                }
            }

            private boolean isItemInCSV(String itemName) {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File csvFile = new File(downloadsDir, csvFileName);

                if (!csvFile.exists()) {
                    Log.e("CSVReader", "CSV file does not exist in Downloads folder");
                    return false;
                }

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().equalsIgnoreCase(itemName.trim())) { // Case-insensitive check
                            reader.close();
                            return true;
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    Log.e("CSVReader", "Error reading CSV file", e);
                }
                return false;
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
        Intent intent = new Intent(MainActivity.this, SharedPrefActivity.class);
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
//                dbHelper.deleteFireStoreData(MainActivity.this,pd);
                // Reference to the Firebase Realtime Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference("invoices");
                databaseReference.child("/").removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this, "Cloud database deleted", Toast.LENGTH_LONG).show();
                            } else {
                                pd.dismiss();
                                Toast.makeText(MainActivity.this, "Failed to delete cloud data", Toast.LENGTH_SHORT).show();
                            }
                        });

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

}