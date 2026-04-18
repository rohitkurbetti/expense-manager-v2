package com.example.myapplication;

import static com.example.myapplication.ExpenseActivity.saveExpenseOnCloud;

import android.Manifest;
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
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapters.CsvAdapter;
import com.example.myapplication.adapters.CustomAdapter;
import com.example.myapplication.adapters.DashboardAdapter;
import com.example.myapplication.adapters.Expense;
import com.example.myapplication.adapters.NestedListAdapter;
import com.example.myapplication.adapters.NestedOtherListAdapter;
import com.example.myapplication.adapters.OtherItemsAdapter;
import com.example.myapplication.adapters.SuggestionAdapter;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.database.ExpenseDbHelper;
import com.example.myapplication.database.SuggestionHelper;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.dtos.ItemModel;
import com.example.myapplication.utils.PDFGeneratorUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MainActivity extends BaseActivity {

    public static final String SHARED_PREFS_FILE = "my_shared_prefs";
    private static final String FIRST_LAUNCH_KEY = "isFirstLaunch";
    private static final int REQUEST_MANAGE_STORAGE = 123;
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static List<CustomItem> itemList;
    private final String csvFileName = "item_list.csv";
    boolean isLoadFromSystem = true;
    DatabaseHelper dbHelper;
    ExpenseDbHelper expenseDbHelper;
    ProgressDialog pd;
    com.itextpdf.kernel.colors.Color headerColor1 = new DeviceRgb(102, 178, 255); //
    com.itextpdf.kernel.colors.Color headerColor = new DeviceRgb(255, 128, 0); // Orange
    com.itextpdf.kernel.colors.Color oddRowColor1 = new DeviceRgb(204, 229, 255); //
    com.itextpdf.kernel.colors.Color oddRowColor = new DeviceRgb(255, 229, 204); // Light Orange
    int total = 0;
    String[] themes = {
            "Red", "Blue", "Green", "Purple", "Orange",
            "Teal", "Pink", "Cyan", "Lime", "Brown",
            "Mint", "Coral", "Steel", "Lavender", "Mustard"
    };
    RecyclerView dashboardRecyclerView;
    DashboardAdapter dashboardAdapter;
    List<ItemModel> dashboardItemList;
    View toggleHandle;
    boolean isExpanded = true;
    private boolean isIconOne = true;
    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private Map<String, Integer[]> otherItemsMap;
    private boolean doubleBackToExitPressedOnce = false;
    private List<CustomItem> otherItemsList;
    private ListView otherListView;
    private TextView bannerOtherItems;
    private TextView selectDateLink;
    private TextView bannerTxt;
    private SharedPreferences sharedPreferences;
    private OtherItemsAdapter otherItemsAdapter;
    private long typingDelayMillis = 70; // Delay between each character (in milliseconds)
    private Handler handler = new Handler();
    private TextView dashboardText;
    private CardView cardDashboard;
    private ImageView exclamationImageView;
    private AutoCompleteTextView etItemName;

    private AutoCompleteTextView autoCompleteTextView;
    private TextInputLayout textInputLayout;
    private SuggestionAdapter suggestionAdapter;
    private SuggestionHelper suggestionHelper;
    private int reportsTotal;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // applyUserTheme();  // Apply before setContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE);

        dbHelper = new DatabaseHelper(getApplicationContext());
        suggestionHelper = new SuggestionHelper(getApplicationContext());


        // Show the hacker console dialog when the app launches
//        showHackerConsoleDialog();

        dashboardRecyclerView = findViewById(R.id.recyclerViewDashboard);
        cardDashboard = findViewById(R.id.cardDashboard);
        dashboardText = findViewById(R.id.dashboardText);
        dashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        toggleHandle = findViewById(R.id.toggleHandle);
        collapseView(cardDashboard);

        toggleHandle.setOnClickListener(v -> {
            if (isExpanded) {
                collapseView(cardDashboard);
            } else {
                expandView(cardDashboard);
            }
            isExpanded = !isExpanded;
        });

        // Example data (replace with DB/Firebase fetch)
        dashboardItemList = new ArrayList<>();


        dashboardText.setText(String.valueOf("Most sold items in last 30 days"));
        // dashboardText.setTypeface(getResources().getFont(R.font.poppins_semibold));

        Map<String, Integer> map = new HashMap<>();

        // Formatter for yyyy-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        // Today's date
        LocalDate toDate = LocalDate.now();
        int selectedPeriodValue = 30;
        String selectedPeriod = sharedPreferences.getString("selectedPeriod", "Last 30 days"); // default
        if (selectedPeriod.equalsIgnoreCase("Last 30 days")) {
            selectedPeriodValue = 30;
        } else if (selectedPeriod.equalsIgnoreCase("Last 60 days")) {
            selectedPeriodValue = 60;
        } else if (selectedPeriod.equalsIgnoreCase("Last 90 days")) {
            selectedPeriodValue = 90;
        } else if (selectedPeriod.equalsIgnoreCase("Last 6 months")) {
            selectedPeriodValue = 180;
        } else if (selectedPeriod.equalsIgnoreCase("Last 1 year")) {
            selectedPeriodValue = 365;
        }
        dashboardText.setText(String.valueOf("Most sold items in " + selectedPeriod));

        // 30 days before today
        LocalDate fromDate = toDate.minusDays(selectedPeriodValue);

        // Format both dates
        String startDateFormatted = fromDate.format(formatter);
        String endDateFormatted = toDate.format(formatter);

        DateTimeFormatter formatterForUI = DateTimeFormatter.ofPattern("dd-MMM-yy");

        String fromDateForUI = fromDate.format(formatterForUI);
        String toDateForUI = toDate.format(formatterForUI);

        Log.i(" date period >> ", startDateFormatted + " <> " + endDateFormatted);

        Cursor cursor = dbHelper.getPeriodRecords(startDateFormatted, endDateFormatted);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double itemVal = 0.0d;
                String jsonItemList = cursor.getString(1);
                total += cursor.getInt(2);
                List<CustomItem> itemList = getParserJsonList(jsonItemList);
                for (CustomItem customItem : itemList) {
                    if (map.containsKey(customItem.getName())) {
                        itemVal = map.get(customItem.getName()) + (int) customItem.getSliderValue();
                    } else {
                        itemVal = (int) customItem.getSliderValue();
                    }
                    map.put(customItem.getName(), (int) itemVal);
                    itemVal = 0.0d;
                }
            }
            System.err.println("map: " + map);
        }

        if (!map.isEmpty()) {
            dashboardItemList.clear();
        } else {
            collapseView(cardDashboard);
        }

        map.forEach((k, v) -> {
            dashboardItemList.add(new ItemModel(k, v));
        });


        // Sort by descending quantity
        Collections.sort(dashboardItemList, new Comparator<ItemModel>() {
            @Override
            public int compare(ItemModel o1, ItemModel o2) {
                return o2.getQuantity() - o1.getQuantity();
            }
        });

        dashboardAdapter = new DashboardAdapter(this, dashboardItemList);
        dashboardRecyclerView.setAdapter(dashboardAdapter);

        exclamationImageView = findViewById(R.id.exclamationImageView);
        exclamationImageView.setOnClickListener(v -> showSummaryDialog(selectedPeriod, fromDateForUI, toDateForUI));

        otherItemsMap = new HashMap<String, Integer[]>();
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // For Dark Mode
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // For Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);  // Follow system setting

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkManageStoragePermission();
        } else {
            checkStoragePermission();
        }


        setDeviceModelInSharedPrefs();


        // Check if app is launched for the first time
        if (isFirstLaunch()) {
            showSetItemPricesDialog();
        }


        FirebaseApp.initializeApp(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        expenseDbHelper = new ExpenseDbHelper(getApplicationContext());
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
                if (!itemList.isEmpty()) {
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

    private void showSummaryDialog(String selectedPeriod, String fromDateForUI, String toDateForUI) {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_summary, null);

        TextView tvSummary = dialogView.findViewById(R.id.tvSummary);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        // Set summary text dynamically if needed
        tvSummary.setText("📊 Dashboard Summary:\n\nData from "
                + selectedPeriod.toLowerCase() + "\n" + fromDateForUI + "  to  " + toDateForUI);

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDashboardAfterInsertion() {
        Map<String, Integer> map = new HashMap<>();

        // Formatter for yyyy-MM-dd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        // Today's date
        LocalDate toDate = LocalDate.now();

        int selectedPeriodValue = 30;
        String selectedPeriod = sharedPreferences.getString("selectedPeriod", "Last 30 days"); // default
        if (selectedPeriod.equalsIgnoreCase("Last 30 days")) {
            selectedPeriodValue = 30;
        } else if (selectedPeriod.equalsIgnoreCase("Last 60 days")) {
            selectedPeriodValue = 60;
        } else if (selectedPeriod.equalsIgnoreCase("Last 90 days")) {
            selectedPeriodValue = 90;
        } else if (selectedPeriod.equalsIgnoreCase("Last 6 months")) {
            selectedPeriodValue = 180;
        } else if (selectedPeriod.equalsIgnoreCase("Last 1 year")) {
            selectedPeriodValue = 365;
        }
        dashboardText.setText(String.valueOf("Most sold items in " + selectedPeriod.toLowerCase()));


        // 30 days before today
        LocalDate fromDate = toDate.minusDays(selectedPeriodValue);

        // Format both dates
        String startDateFormatted = fromDate.format(formatter);
        String endDateFormatted = toDate.format(formatter);

        DateTimeFormatter formatterForUI = DateTimeFormatter.ofPattern("dd-MMM-yy");

        String fromDateForUI = fromDate.format(formatterForUI);
        String toDateForUI = toDate.format(formatterForUI);
        Log.i(" date period >> ", startDateFormatted + " <> " + endDateFormatted);
        Cursor cursor = dbHelper.getPeriodRecords(startDateFormatted, endDateFormatted);

        if (cursor.getCount() > 0) {
            dashboardItemList.clear();
            while (cursor.moveToNext()) {
                double itemVal = 0.0d;
                String jsonItemList = cursor.getString(1);
                total += cursor.getInt(2);
                List<CustomItem> itemList = getParserJsonList(jsonItemList);
                for (CustomItem customItem : itemList) {
                    if (map.containsKey(customItem.getName())) {
                        itemVal = map.get(customItem.getName()) + (int) customItem.getSliderValue();
                    } else {
                        itemVal = (int) customItem.getSliderValue();
                    }
                    map.put(customItem.getName(), (int) itemVal);
                    itemVal = 0.0d;
                }
            }
            System.err.println("map: " + map);
        }

        if (map.isEmpty()) {
            dashboardItemList.clear();
            dashboardAdapter.notifyDataSetChanged();
        }

        map.forEach((k, v) -> {
            dashboardItemList.add(new ItemModel(k, v));
        });

        // Sort by descending quantity
        Collections.sort(dashboardItemList, new Comparator<ItemModel>() {
            @Override
            public int compare(ItemModel o1, ItemModel o2) {
                return o2.getQuantity() - o1.getQuantity();
            }
        });

        dashboardAdapter.updateData(dashboardItemList);
//        if (isExpanded) {
//            collapseView(cardDashboard);
//        } else {
//        expandView(cardDashboard);
//        }
//        isExpanded = !isExpanded;
    }

    private void collapseView(View view) {
        int initialHeight = view.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            view.getLayoutParams().height = value;
            view.requestLayout();
        });
        animator.setDuration(300);
        animator.start();
    }

    private void expandView(View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int targetHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), targetHeight);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            view.getLayoutParams().height = value;
            view.requestLayout();
        });
        animator.setDuration(300);
        animator.start();
    }

    // private void applyUserTheme() { ... } removed


    private void showHackerConsoleDialog() {
        HackerConsoleDialog dialog = new HackerConsoleDialog(this);
        dialog.show();

        // The sentences you want to auto-type, one per array element
        String[] automatedTasks = new String[]{
                "System initializing...",
                "Establishing secure connection...",
                "Accessing encrypted data streams...",
                "Running diagnostic protocols...",
                "Initialization complete...",
                "Welcome, Anonymous user."
        };
        dialog.startTyping(automatedTasks);
    }

    private void setDeviceModelInSharedPrefs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("model", Build.MODEL.contains("samsung") ? "samsung" :
                Build.MODEL.contains("moto g84 5G") ? "moto" :
                        Build.MODEL.contains("Redmi Note 9 Pro") ? "redmi" : Build.MODEL);
        editor.apply();
    }


    private boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(this, "resume() called", Toast.LENGTH_SHORT).show();
        // Refresh your dashboard data here

        updateDashboardAfterInsertion();
        dashboardAdapter.notifyDataSetChanged();
        if (dashboardItemList.isEmpty()) {
            collapseView(cardDashboard);
        }

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
        if (coldDrinks != null && coldDrinks.size() > 0) {
            menuItem.setTitle("Load Items from System");
            isLoadFromSystem = false;
            itemList.clear();
            coldDrinks.forEach(item -> {
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
                String text = editText.getText().toString();
                stringBuilder.append(text);
                if (text != null && !text.equals("")) {
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
        if (loadItemsFromInteralStorage(itemList, csvFileName)) {
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
            itemList.add(new CustomItem("Pineapple Soda", false, 0));
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
        if (itemList.size() == 0) {
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
            Toast.makeText(this, "Inserted " + items.size() + " items", Toast.LENGTH_SHORT).show();
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
                coldDrinks.add(line.replace(",", ""));
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
                            Long total = document.getLong("total");
                            String createdDate = document.getString("created_date");
                            String createdDateTime = document.getString("created_date_time");
                            String jsonList = document.getString("item_list_json");

                            stringBuilder.append(id + " " + jsonList + " " + total + " " + createdDateTime + " " + createdDate + "\n\n\n\n|=================================|\n\n\n\n");

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
                        Toast.makeText(MainActivity.this, "Error getting documents from collection " + invoices, Toast.LENGTH_SHORT).show();
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
        if (stringBuildercontent != null) {
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
        itemList = itemList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());

        if (itemList.size() > 0) {

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

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");

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
                                String monthName = Month.of(selectedMonth + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                                String selectedDate = selectedDay + "-" + monthName + "-" + selectedYear;
                                String strFmtDay = String.format("%02d", selectedDay);
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
                    if (isChecked) {
//                        TextView newTextView = new TextView(MainActivity.this);
//                        newTextView.setText("New Dynamic View");
//                        newTextView.setPadding(0, 20, 0, 0);
//                        newTextView.setTextSize(16);

//                        otherEntityView.setVisibility(View.VISIBLE);
                        Map<String, Integer[]> itemsMap = showOtherEntityAlert(null, false, checkBox);
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


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialog1 -> {
                Button openNested = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                openNested.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Confirm date?")
                                .setIcon(R.drawable.cloud_computing_enabled_data)
                                .setCancelable(false)
                                .setMessage("Save invoice for " + selectDateLink.getText())
                                .setPositiveButton("Save", (asd, which) -> {
                                    //map to list;
                                    otherItemsList = new ArrayList<>();

                                    if (otherItemsMap != null && otherItemsMap.size() > 0) {
                                        otherItemsMap.forEach((k, val) -> {
                                            CustomItem customItem = new CustomItem(k, false, val[1], val[0] * val[1]);
                                            otherItemsList.add(customItem);
                                        });
                                    }

                                    try {
                                        calculate(finalItemList, otherItemsList, selectDateLink.getText().toString(), false);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    otherItemsList.clear();
                                    dialog.dismiss();
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
            });

            dialog.show();
        } else {
            Toast.makeText(this, "Please select minimum 1 item", Toast.LENGTH_SHORT).show();

            Boolean otherItemsOnly = true;
            showOtherEntityAlert(null, otherItemsOnly, null);


        }
    }

    private Map<String, Integer[]> showOtherEntityAlert(TextView otherTextView, Boolean otherItemsOnly, CheckBox checkBox) {
        AtomicInteger qty = new AtomicInteger(0);
        View view = getLayoutInflater().inflate(R.layout.other_entity, null, false);

        Button btnAddToBucket = view.findViewById(R.id.btnAddToBucket);
        ImageView btnDelSpinnerItem = view.findViewById(R.id.btnDelSpinnerItem);
        Spinner spinnerBucket = view.findViewById(R.id.spinnerBucket);
        etItemName = view.findViewById(R.id.etItemName);

        setupAutoComplete();


        // Set long click listener for AutoCompleteTextView
        setupAutoCompleteLongPress();

        List<String> updateList = suggestionHelper.getAllSuggestions();

        suggestionAdapter.updateSuggestionList(updateList);

        EditText etItemValue = view.findViewById(R.id.etItemValue);
        EditText etOtherItemQty = view.findViewById(R.id.otherItemQty);
        ImageView otherQtyUpBtn = view.findViewById(R.id.otherQtyUpBtn);
        ImageView otherQtyDownBtn = view.findViewById(R.id.otherQtyDownBtn);

        ListView otherItemsListView = view.findViewById(R.id.otherItemsListView);


        Set<CustomItem> customItemSet = new HashSet<>();
        List<CustomItem> otherItemsF = new ArrayList<>(customItemSet);

        otherItemsAdapter = new OtherItemsAdapter(this, otherItemsF);
        otherItemsListView.setAdapter(otherItemsAdapter);


        otherQtyUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etOtherItemQty.setText(String.valueOf(qty.addAndGet(1)));
            }
        });

        otherQtyDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qty.get() > 0) {
                    etOtherItemQty.setText(String.valueOf(qty.decrementAndGet()));
                } else {
                    etOtherItemQty.setText(String.valueOf(qty.getAndSet(1)));
                }

            }
        });


        List<String> items = new ArrayList<>();


        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBucket.setAdapter(spinnerAdapter);

        Map<String, Integer[]> itemMap = new HashMap<>();

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
                int qty = Integer.parseInt(etOtherItemQty.getText().toString());

                if (itemName != null && !itemValue.isEmpty() && itemValue != null && !itemValue.isEmpty() && qty != 0) {
//                    addItemToSpinner(itemName, itemValue);
                    addItemToSet(itemName, itemValue, qty);
                    showCustomToast("Item " + itemName + " added");
//                    animateBackground(spinnerBucket, false);
                    otherItemsF.clear();
                    otherItemsF.addAll(customItemSet);
                    Log.i("Other Items >> ", customItemSet.toString());

                }
            }

            private void addItemToSet(String itemName, String itemRate, Integer qty) {
                CustomItem customItem = new CustomItem(itemName, false, (float) qty, Integer.parseInt(itemRate));
                if (!customItemSet.add(customItem)) {
                    customItemSet.remove(customItem);
                    customItemSet.add(new CustomItem(itemName, false, (float) qty, Integer.parseInt(itemRate)));
                }

                int val = Integer.parseInt(itemRate);

                itemMap.put(itemName, new Integer[]{val, qty});
                etItemName.setText("");
                etItemValue.setText("");
                etOtherItemQty.setText("0");

                otherItemsAdapter.notifyDataSetChanged();
            }

            private void addItemToSpinner(String itemName, String itemValue) {
                items.clear();
//                itemMap.put(itemName, Integer.valueOf(itemValue));

                if (!itemMap.containsKey(itemName)) {
                    int rate = Integer.parseInt(itemValue);
                    customItemSet.add(new CustomItem(itemName, false, (float) qty.get(), rate));
                }

                itemMap.forEach((k, v) -> {
                    items.add(k + " = " + v);
                });
                etItemName.setText("");
                etItemValue.setText("");
                etOtherItemQty.setText("0");
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
                    showCustomToast("Item " + itemName.split(" = ")[0] + " removed");

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
//                String jsonString = gson.toJson(itemMap);

                System.err.println("MAP json >>> " + itemMap);

                otherItemsMap.putAll(itemMap);
                otherItemsList = new ArrayList<>();
//                if(otherItemsOnly){


                if (otherItemsMap != null && otherItemsMap.size() > 0) {
                    otherItemsMap.forEach((k, v) -> {
                        CustomItem customItem = new CustomItem(k, false, v[1], (v[0] * v[1]));
                        otherItemsList.add(customItem);
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
                        // Use the editor to put values into SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(k.toUpperCase(Locale.getDefault()), v[0]);
                        // Commit the changes
                        editor.apply();
                    });
                    try {
                        if (otherItemsOnly) {
                            calculate(new ArrayList<>(), otherItemsF, null, otherItemsOnly);
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
                if (otherItemsList != null && otherItemsList.size() > 0) {
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

                    NestedOtherListAdapter nestedOtherListAdapter = new NestedOtherListAdapter(builder.getContext(), otherItemsList, otherListView, checkBox, bannerOtherItems);
                    otherListView.setAdapter(nestedOtherListAdapter);

                    nestedOtherListAdapter.notifyDataSetChanged();

                } else {
//                    otherListView.setVisibility(View.GONE);
//                    bannerOtherItems.setVisibility(View.GONE);
                    if (checkBox != null) {
                        checkBox.setChecked(false);
                    }
                }
            }
        });

        dialog.show();
        return itemMap;
    }

    private void setupAutoCompleteLongPress() {
        etItemName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAddSuggestionPopup();
                return true; // Consume the long click event
            }
        });
    }

    private void showAddSuggestionPopup() {
        // Get current text from AutoCompleteTextView
        String currentText = etItemName.getText().toString().trim();

        // Create options for the popup
        final String[] options = {"Add new suggestion"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suggestion Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Add new suggestion
                        showAddSuggestionDialog(currentText);
                        break;
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddSuggestionDialog(String initialText) {
        // Create dialog with custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Suggestion");

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_suggestion, null);
        builder.setView(dialogView);

        // Get reference to EditText
        final EditText etSuggestion = dialogView.findViewById(R.id.etSuggestion);

        // Set initial text if available
        if (initialText != null && !initialText.isEmpty()) {
            etSuggestion.setText(initialText);
            etSuggestion.setSelection(initialText.length()); // Place cursor at end
        }

        // Set up buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // This will be overridden to prevent automatic dismissal
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newSuggestion = etSuggestion.getText().toString().trim();

                if (newSuggestion.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a suggestion", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newSuggestion.length() < 2) {
                    Toast.makeText(MainActivity.this, "Suggestion too short", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add the suggestion to database
                addNewSuggestion(newSuggestion);
                dialog.dismiss();
            }
        });
    }

    private void addNewSuggestion(String newSuggestion) {

        if (newSuggestion == null || newSuggestion.trim().isEmpty()) {
            return;
        }

        new AsyncTask<String, Void, Boolean>() {
            StringBuilder stringBuilder = new StringBuilder();

            @Override
            protected Boolean doInBackground(String... suggestions) {
                try {
                    long result = suggestionHelper.insertSuggestion(suggestions[0].trim());
                    return result != -1;
                } catch (Exception e) {
                    Log.e("AddSuggestion", "Error adding suggestion: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "Suggestion added successfully!", Toast.LENGTH_SHORT).show();
                    // Reload suggestions
                    new LoadSuggestionsTask().execute();
                    for (int i = 0; i < suggestionAdapter.getCount(); i++) {
                        stringBuilder.append(suggestionAdapter.getItem(i) + "\n");
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Suggestion already exists!", Toast.LENGTH_SHORT).show();
                }

                List<String> updateList = suggestionHelper.getAllSuggestions();

                suggestionAdapter.updateSuggestionList(updateList);


            }
        }.execute(newSuggestion);

    }

    private void setupAutoComplete() {


        // Create adapter with mutable list
        suggestionAdapter = new SuggestionAdapter(this, InvoiceConstants.SUGGESTIONS);
        etItemName.setAdapter(suggestionAdapter);

        // Customize dropdown appearance
        etItemName.setDropDownBackgroundDrawable(getResources().getDrawable(R.drawable.dropdown_background));
        etItemName.setDropDownVerticalOffset(8);
        etItemName.setDropDownHorizontalOffset(0);
        etItemName.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set threshold - show suggestions after 1 character
        etItemName.setThreshold(1);

        // Optional: Add item click listener
        etItemName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            etItemName.setText(selectedItem);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculate(List<CustomItem> itemList, List<CustomItem> otherItemsList, String selectDate, Boolean otherItemsOnly) throws IOException {
        itemList = itemList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());
        otherItemsList = otherItemsList.stream().filter(i -> i.getSliderValue() > 0.0f).collect(Collectors.toList());

        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("dd-MMMM-yyyy").toFormatter();
        if (selectDate == null) {
            selectDate = String.valueOf(LocalDate.now());
        }
        LocalDate localDate = LocalDate.now();
        if (otherItemsOnly) {
            localDate = LocalDate.parse(selectDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            localDate = LocalDate.parse(selectDate, dateTimeFormatter);
        }
        if (itemList.size() > 0 || otherItemsList.size() > 0) {

            Long grandTotal = getTotal(itemList, otherItemsList);

            DtoJson dtoJson = new DtoJson();
            dtoJson.setName(null);
            dtoJson.setDate(localDate.toString());
            dtoJson.setCreateddtm(createDtmFromLocalDate(localDate));
            dtoJson.setTotal(grandTotal);
            dtoJson.setItemList(itemList);
            dtoJson.setOtherItemsList(otherItemsList);

            String dtoJsonStr = convertCustomItemsToJson(dtoJson);
            System.err.println(dtoJsonStr);

            long newRowId = dbHelper.saveInvoiceTransaction("\"" + dtoJsonStr + "\"", grandTotal, MainActivity.this, dtoJson, null);
            if (newRowId != -1) {
                updateDashboardAfterInsertion();
                writeTextFile(dtoJsonStr, newRowId);
                File pdfFile = PDFGeneratorUtil.generateInvoice(dtoJson, newRowId, getApplicationContext());
                View rootView = findViewById(android.R.id.content);
                Snackbar.make(rootView, "Invoice generated " + newRowId, Snackbar.LENGTH_LONG)
                        .setDuration(5000)
                        .setAction("View", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openGeneratedPDF(pdfFile);
                                // Create an Intent to open the generated invoice
                            }
                        })
                        .show();
                writeAllDbContentInTxtFile(dtoJson, dbHelper);
                resetSliders();
            }
        } else {
            Toast.makeText(this, "Please select minimum 1 item", Toast.LENGTH_SHORT).show();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createDtmFromLocalDate(LocalDate localDate) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDate.toString() + " " + String.format("%02d", localDateTime.getHour()) + ":"
                + String.format("%02d", localDateTime.getMinute()) + ":" + String.format("%02d", localDateTime.getSecond());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateInvoice(DtoJson dtoJson, long newRowId) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

            String pdfPath = pdfPathMain + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "invoices";

            if (!Files.exists(Paths.get(pdfPath))) {
                new File(pdfPath).mkdirs();
            }

            LocalDateTime localDateTime = LocalDateTime.now();
            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            File pdfFile = new File(pdfPath, "invoice_" + frmtted + ".pdf");
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
            div1.setMargins(10, 5, 10, 5);


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
            tableTe.addCell(new Cell().setWidth((pdfDocument.getDefaultPageSize().getWidth() / 2)).setBorder(Border.NO_BORDER).add(headingTitle));
            tableTe.addCell(new Cell().setWidth((pdfDocument.getDefaultPageSize().getWidth() / 2)).setBorder(Border.NO_BORDER).add(title));

            // Add the table to the document
            document.add(tableTe);
            document.add(div1);

//            div1.add(headingTitle);
//            div2.add(title);

//            div1.setMarginRight(10);
//            div2.setMarginLeft(columnWidth + 10);

//            document.add(div2);


            String name = dtoJson.getName();

            if (name == null) {
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
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(sharedPreferences.getInt(item.getName().toUpperCase(Locale.getDefault()), 0)))));
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
            Snackbar.make(rootView, "Invoice generated " + newRowId, Snackbar.LENGTH_LONG)
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
        dbHelper.getAllDbRecords(dtoJson, MainActivity.this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeTextFile(String dtoJsonStr, long newRowId) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        String subPath = path + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "json data";

        if (!Files.exists(Paths.get(subPath))) {
            new File(subPath).mkdirs();
        }
        String dateFrmtted = new SimpleDateFormat("ddMMMyy_HHmmss").format(new Date());
        String filePath = subPath + File.separator + "Invoice_Dtl_Jsn_" + newRowId + "_" + dateFrmtted + ".txt";
        try {
            // Create a BufferedWriter to write to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            // Write the content to the file
            writer.write(dtoJsonStr);
            // Close the writer
            writer.close();
        } catch (IOException e) {
            Toast.makeText(this, "Error writing to the file: ", Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.menu, menu);
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
            restoreAllData();
//                restoreFromBackedUpCsv();
//                restoreFromBackedUpExpensesCsv();
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
            new AlertDialog.Builder(this)
                    .setTitle("Delete All Data")
                    .setMessage("Are you sure you want to delete all app data? This action cannot be undone.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes, Delete", (dialog, which) -> {
                        dbHelper.deleteAllData();
                        expenseDbHelper.deleteAllData();
                        Toast.makeText(this, "All app data deleted", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }


        if (id == R.id.toggle_cloud_store) {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
            // Use the editor to put values into SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("toggleCloudStore", !sharedPreferences.getBoolean("toggleCloudStore", false));
            editor.apply();

            String cloudStorageState = "Off";
            if (sharedPreferences.getBoolean("toggleCloudStore", false)) {
                cloudStorageState = "On";
                item.setIcon(R.drawable.cloud_computing_enabled_data);  // Switch to icon two
            } else {
                cloudStorageState = "Off";
                item.setIcon(R.drawable.cloud_computing_nodata);  // Switch back to icon one
            }
            isIconOne = !isIconOne;

            Toast.makeText(this, "Cloud storage: " + cloudStorageState, Toast.LENGTH_SHORT).show();
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

        if (id == R.id.invalidateExpenses) {

            String date = expenseDbHelper.findMinExpDate();
            if (date == null || date == "") {
                Toast.makeText(this, "Date is invalid", Toast.LENGTH_SHORT).show();
                return false;
            }
            List<String> dates = getDatesFromNextDayToTodayMain(date);

            dates.forEach(currDate -> {

                Cursor res = expenseDbHelper.checkIfExpenseExists(currDate);
                if (res != null && res.getCount() > 0) {
                    while (res.moveToNext()) {
                        int expId = res.getInt(0);
                        String expPart = res.getString(1);
                        int expAmount = res.getInt(2);
                        String expDateTime = res.getString(3);
                        String expDate = res.getString(4);


                        LocalDate yesterDays = LocalDate.now();
                        if (StringUtils.isNotEmpty(currDate)) {
                            LocalDate dateParsed = LocalDate.parse(currDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            yesterDays = dateParsed.minusDays(1);
                        }

                        long yesterdaysBalanceUpdated = expenseDbHelper.getYesterdaysBalance(String.valueOf(yesterDays));
                        long todaysSalesUpdated = dbHelper.getTodaysSales(currDate);
                        long balanceUpdated = (yesterdaysBalanceUpdated + todaysSalesUpdated) - ((long) expAmount);

                        long rowsAffected = expenseDbHelper.updateExpenseYesterdaysBalanceAndSales(expId, expDate, yesterdaysBalanceUpdated, todaysSalesUpdated, balanceUpdated);

                        // Check if the update was successful
                        if (rowsAffected > 0) {
                            Toast.makeText(this, "Record updated successfully " + expId, Toast.LENGTH_SHORT).show();
                            Expense exp = new Expense(expId, expPart, expAmount, expDateTime, expDate, (int) yesterdaysBalanceUpdated, (int) todaysSalesUpdated, (int) balanceUpdated);
                            saveExpenseOnCloud(this, expDate, exp);
                        } else {
                            Toast.makeText(this, expId + " Update failed. No matching record found.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
            return true;
        }

        if (id == R.id.expenseSummary) {
            Intent intent = new Intent(MainActivity.this, ExpenseSummaryActivity.class);
            startActivity(intent);
        }

        if (id == R.id.chooseTheme) {
            showThemeChooserDialog();
            return true;
        }

        if (id == R.id.newUI) {
            launchNewUI();
            return true;
        }

        if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.cool) {
            Intent intent = new Intent(this, MainActivity2.class);
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
    private void restoreAllData() {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Restoring data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Use AtomicInteger to track completion of both tasks
        java.util.concurrent.atomic.AtomicInteger completed = new java.util.concurrent.atomic.AtomicInteger(0);
        int[] invoicesCount = new int[1];
        int[] expensesCount = new int[1];

        try {
            restoreFromBackedUpCsv(count -> {
                invoicesCount[0] = count;
                if (completed.incrementAndGet() == 2) {
                    showCombinedDialog(invoicesCount[0], expensesCount[0], progressDialog);
                }
            });
        } catch (IOException e) {
            invoicesCount[0] = -1;
            if (completed.incrementAndGet() == 2) {
                showCombinedDialog(invoicesCount[0], expensesCount[0], progressDialog);
            }
        }

        try {
            restoreFromBackedUpExpensesCsv(count -> {
                expensesCount[0] = count;
                if (completed.incrementAndGet() == 2) {
                    showCombinedDialog(invoicesCount[0], expensesCount[0], progressDialog);
                }
            });
        } catch (IOException e) {
            expensesCount[0] = -1;
            if (completed.incrementAndGet() == 2) {
                showCombinedDialog(invoicesCount[0], expensesCount[0], progressDialog);
            }
        }
    }

    private void showCombinedDialog(int invoicesRestored, int expensesRestored, ProgressDialog progressDialog) {
        progressDialog.dismiss();

        StringBuilder sb = new StringBuilder();
        if (invoicesRestored == -1) {
            sb.append("❌ Invoices backup file not found.\n");
        } else {
            sb.append("📄 Invoices restored: ").append(invoicesRestored).append(" rows\n");
        }
        if (expensesRestored == -1) {
            sb.append("❌ Expenses backup file not found.\n");
        } else {
            sb.append("💰 Expenses restored: ").append(expensesRestored).append(" rows\n");
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Restore Summary")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();

        Log.i("RestoreAll", sb.toString().replace("\n", " | "));
    }

    private void launchNewUI() {

        Intent intent = new Intent(this, NewUIActivity.class);
        startActivity(intent);

    }

    private void showThemeChooserDialog() {
        String currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("app_theme", "Theme.Base.MyApplication");

        int checkedIndex = Arrays.asList(themes).indexOf(currentTheme);

        new AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themes, checkedIndex, (dialog, which) -> {
                    String selectedTheme = themes[which];

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putString("app_theme", selectedTheme);
                    editor.apply();
                    dialog.dismiss();

                    Intent intent = getIntent();
                    finish(); // Destroy current activity
                    startActivity(intent); // Start it again
                })
                .setNegativeButton("Cancel", null)
                .show();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void restoreFromBackedUpExpensesCsv(Consumer<Integer> onComplete) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "ExpensesBackup.csv";
        File docsDir = new File(filePath);

        if (!docsDir.exists()) {
            Log.e("CSVReaderUtil", "File not found: " + docsDir.getAbsolutePath());
            if (onComplete != null) onComplete.accept(-1);
            return;
        }

        new Thread(() -> {
            int counter = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(docsDir))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (columns.length >= 8) {
                        int expId = Integer.parseInt(columns[0].trim());
                        String expPart = columns[1].trim().replace("\"", "");
                        int expAmt = Integer.parseInt(columns[2].trim());
                        String expCreatedDateTime = columns[3].trim();
                        String expCreatedDate = columns[4].trim();
                        int expYesterdaysBalance = Integer.parseInt(columns[5].trim());
                        int expSales = Integer.parseInt(columns[6].trim());
                        int expBalance = Integer.parseInt(columns[7].trim());

                        boolean expExists = expenseDbHelper.checkIfExpenseExistsFlag(expCreatedDate);
                        if (!expExists) {
                            boolean inserted = expenseDbHelper.restoreExpense(expId, expPart, expAmt, expCreatedDateTime, expCreatedDate,
                                    expYesterdaysBalance, expSales, expBalance);
                            if (inserted) counter++;
                        }

                        // Cloud sync (unchanged)
                        String deviceModel = sharedPreferences.getString("model", Build.MODEL);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel + "/expenses");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate date = LocalDate.parse(expCreatedDate, formatter);
                        String formattedYearMonth = date.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
                        String childPath = "/" + date.getYear() + "/" + formattedYearMonth + "/" + date;
                        Expense expense = new Expense(expId, expPart, expAmt, expCreatedDateTime, expCreatedDate,
                                expYesterdaysBalance, expSales, expBalance);
                        databaseReference.child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    databaseReference.child(childPath).setValue(expense);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e("RestoreExpenses", "Error", e);
                if (onComplete != null) onComplete.accept(-1);
                return;
            }

            final int finalCounter = counter;
            runOnUiThread(() -> {
                if (onComplete != null) onComplete.accept(finalCounter);
            });
        }).start();
    }


//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void restoreFromBackedUpExpensesCsv() throws IOException {
//        List<Expense> expenses = new ArrayList<>();
//
//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//        String filePath = path + File.separator + "ExpensesBackup.csv";
//
//        File docsDir = new File(filePath);
//
//        if (!docsDir.exists()) {
//            Log.e("CSVReaderUtil", "File not found: " + docsDir.getAbsolutePath());
//        }
//
//
//        // Read CSV file
//        BufferedReader reader = new BufferedReader(new FileReader(docsDir));
//        String line;
//        boolean firstLine = true;
//        int counter = 0;
//        while ((line = reader.readLine()) != null) {
////            if (firstLine) {
////                firstLine = false; // Skip header line
////                continue;
////            }
//
//            // Split CSV by commas, handling JSON strings with quotes
//            String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//
//            if (columns.length >= 8) {
//                int expId = Integer.parseInt(columns[0].trim());
//                String expPart = columns[1].trim();
//                int expAmt = Integer.parseInt(columns[2].trim());
//                String expCreatedDateTime = columns[3].trim();
//                String expCreatedDate = columns[4].trim();
//                int expYesterdaysBalance = Integer.parseInt(columns[5].trim());
//                int expSales = Integer.parseInt(columns[6].trim());
//                int expBalance = Integer.parseInt(columns[7].trim());
//
//                expPart = expPart.replace("\"", "");
//
//                Expense expense = new Expense(expId, expPart, expAmt, expCreatedDateTime, expCreatedDate, expYesterdaysBalance, expSales, expBalance);
//
//                boolean expExists = expenseDbHelper.checkIfExpenseExistsFlag(expCreatedDate);
//
//                if (!expExists) {
//
//                    boolean isInserted = expenseDbHelper.restoreExpense(expId, expPart, expAmt, expCreatedDateTime, expCreatedDate, expYesterdaysBalance, expSales, expBalance);
//
//                    if (isInserted) {
//                        ++counter;
//                        //save expense on cloud
////                        saveExpenseOnCloud(this, expense.getExpenseDate(), expense);
//                    }
//                }
//
//                //check if entry exists in cloud
//
//                String deviceModel = sharedPreferences.getString("model", Build.MODEL);
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel + "/" + "expenses"); // Replace 'items' with your specific path
//
//                String childPath = "";
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate date = LocalDate.parse(expCreatedDate, formatter);
//
//                String formattedYearMonth = date.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
//
//                int year = date.getYear();
//
//
//                childPath = "/" + year + "/" + (formattedYearMonth) + "/" + date;
//
//                String finalChildPath = childPath;
//                databaseReference.child(finalChildPath).addListenerForSingleValueEvent(new ValueEventListener() {
//
//
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.hasChildren()) {
//                            boolean dataExists = false;
////                                    for (DataSnapshot d3 : snapshot.getChildren()) {
//                            Expense exp = snapshot.getValue(Expense.class);
//                            if (
//                                    Objects.equals(exp.getExpenseAmount(), expAmt) &&
//                                            exp.getExpenseDateTime().equalsIgnoreCase(expCreatedDateTime) &&
//                                            exp.getExpenseDate().equalsIgnoreCase(expCreatedDate)
//                            ) {
////                                    Toast.makeText(MainActivity.this, "Entry exists", Toast.LENGTH_SHORT).show();
//                                dataExists = true;
////                                            break;
//                            } else {
//                                dataExists = false;
//                            }

    /// /                                    }
//
//                            if (!dataExists) {
//                                // Store user data
//                                databaseReference.child(finalChildPath).setValue(expense)
//                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void unused) {
//                                                Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                            }
//
//                        } else {
//                            databaseReference.child(finalChildPath).setValue(expense);
//
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//
//
//            }
//        }
//        Toast.makeText(this, "[Expenses] Restored " + counter + " rows", Toast.LENGTH_SHORT).show();
//        reader.close();
//
//    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<String> getDatesFromNextDayToTodayMain(String inputDate) {
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void restoreFromBackedUpCsv(Consumer<Integer> onComplete) throws IOException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        String filePath = path + File.separator + "InvoicesBackup.csv";
        File docsDir = new File(filePath);

        if (!docsDir.exists()) {
            Log.e("CSVReaderUtil", "File not found: " + docsDir.getAbsolutePath());
            if (onComplete != null) onComplete.accept(-1); // -1 indicates file missing
            return;
        }

        new Thread(() -> {
            int counter = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(docsDir));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    if (columns.length >= 5) {
                        long invoiceId = Long.parseLong(columns[0].trim());
                        String itemListJson = columns[1].trim();
                        long total = Long.parseLong(columns[2].trim());
                        String createdDateTime = columns[3].trim();
                        String createdDate = columns[4].trim();

                        DtoJson dtoJson = new DtoJson();
                        dtoJson.setCreateddtm(createdDateTime);
                        dtoJson.setDate(createdDate);
                        dtoJson.setTotal(total);

                        boolean exists = dbHelper.entryExists(invoiceId, total);
                        if (!exists) {
                            long newRowId = dbHelper.saveInvoiceTransaction(itemListJson, total, MainActivity.this, dtoJson, invoiceId);
                            if (newRowId != -1) counter++;
                        }

                        // Cloud sync (unchanged)
                        String deviceModel = sharedPreferences.getString("model", Build.MODEL);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel + "/invoices");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(createdDateTime, formatter);
                        String formattedYearMonth = dateTime.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
                        String time = dateTime.toLocalTime().toString().replace(":", "_");
                        String childPath = "/" + dateTime.getYear() + "/" + formattedYearMonth + "/" + dateTime.toLocalDate() + "/" + time;
                        String finalCreatedDateTime = createdDateTime;
                        String finalChildPath = childPath;
                        Long finalInvoiceId = invoiceId;
                        databaseReference.child(finalChildPath).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChildren()) {
                                    DtoJsonEntity dtoJsonEntity = snapshot.getValue(DtoJsonEntity.class);
                                    if (dtoJsonEntity != null &&
                                            Objects.equals(dtoJsonEntity.getInvoiceId(), finalInvoiceId) &&
                                            Objects.equals(dtoJsonEntity.getTotal(), total) &&
                                            dtoJsonEntity.getCreateddtm().equalsIgnoreCase(finalCreatedDateTime) &&
                                            dtoJsonEntity.getDate().equalsIgnoreCase(createdDate)) {
                                        // exists, do nothing
                                    } else {
                                        DtoJsonEntity entity = new DtoJsonEntity(finalInvoiceId, dtoJson.getName(), total, finalCreatedDateTime, createdDate, itemListJson);
                                        databaseReference.child(finalChildPath).setValue(entity);
                                    }
                                } else {
                                    DtoJsonEntity entity = new DtoJsonEntity(finalInvoiceId, dtoJson.getName(), total, finalCreatedDateTime, createdDate, itemListJson);
                                    databaseReference.child(finalChildPath).setValue(entity);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            } catch (IOException e) {
                Log.e("RestoreInvoices", "Error", e);
                if (onComplete != null) onComplete.accept(-1);
                return;
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                }
            }

            final int finalCounter = counter;
            runOnUiThread(() -> {
                if (onComplete != null) onComplete.accept(finalCounter);
            });
        }).start();
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void restoreFromBackedUpCsv() throws IOException {
//
//        List<DtoJsonEntity> invoices = new ArrayList<>();
//
//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//        String filePath = path + File.separator + "InvoicesBackup.csv";
//
//        File docsDir = new File(filePath);
//
//        if (!docsDir.exists()) {
//            Log.e("CSVReaderUtil", "File not found: " + docsDir.getAbsolutePath());
//        }
//
//        new Thread(new Runnable() {
//            private int counter = 0;
//
//            @Override
//            public void run() {
//                // Notify on the main thread
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "Restoring is started !", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//                // Read CSV file
//                BufferedReader reader = null;
//                try {
//                    reader = new BufferedReader(new FileReader(docsDir));
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                }
//                String line;
//                boolean firstLine = true;
//                String createdDateTime;
//                Long invoiceId;
//                while (true) {
//                    try {
//                        if (!((line = reader.readLine()) != null)) break;
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
////            if (firstLine) {
////                firstLine = false; // Skip header line
////                continue;
////            }
//
//                    // Split CSV by commas, handling JSON strings with quotes
//                    String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//
//                    if (columns.length >= 5) {
//                        invoiceId = Long.parseLong(columns[0].trim());
//                        String itemListJson = columns[1].trim();
//                        Long total = Long.parseLong(columns[2].trim());
//                        createdDateTime = columns[3].trim();
//                        String createdDate = columns[4].trim();
//
//                        DtoJson dtoJson = new DtoJson();
//                        dtoJson.setCreateddtm(createdDateTime);
//                        dtoJson.setDate(createdDate);
//                        dtoJson.setTotal(total);
//
//                        boolean exists = dbHelper.entryExists(invoiceId, total);
//
//                        if (!exists) {
//                            long newRowId = dbHelper.saveInvoiceTransaction(itemListJson, total, MainActivity.this, dtoJson, invoiceId);
//                            if (newRowId != -1) {
//                                ++counter;
//                            }
//                        }
//
//                        //check if entry exists in cloud
//
//                        String deviceModel = sharedPreferences.getString("model", Build.MODEL);
//                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel + "/" + "invoices"); // Replace 'items' with your specific path
//
//                        String childPath = "";
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                        LocalDateTime dateTime = LocalDateTime.parse(createdDateTime, formatter);
//
//                        String formattedYearMonth = dateTime.format(DateTimeFormatter.ofPattern("MMM-yyyy"));
//
//                        int year = dateTime.getYear();
//                        String time = dateTime.toLocalTime().toString();
//                        time = time.replace(":", "_");
//
//
//                        childPath = "/" + year + "/" + (formattedYearMonth) + "/" + dateTime.toLocalDate() + "/" + time;
//
//                        String finalCreatedDateTime = createdDateTime;
//                        String finalChildPath = childPath;
//                        Long finalInvoiceId = invoiceId;
//                        databaseReference.child(finalChildPath).addListenerForSingleValueEvent(new ValueEventListener() {
//
//
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                if (snapshot.hasChildren()) {
//                                    boolean dataExists = false;
////                                    for (DataSnapshot d3 : snapshot.getChildren()) {
//                                    DtoJsonEntity dtoJsonEntity = snapshot.getValue(DtoJsonEntity.class);
//                                    if (
//                                            Objects.equals(dtoJsonEntity.getInvoiceId(), finalInvoiceId) &&
//                                                    Objects.equals(dtoJsonEntity.getTotal(), total) &&
//                                                    dtoJsonEntity.getCreateddtm().equalsIgnoreCase(finalCreatedDateTime) &&
//                                                    dtoJsonEntity.getDate().equalsIgnoreCase(createdDate)
//                                    ) {
////                                    Toast.makeText(MainActivity.this, "Entry exists", Toast.LENGTH_SHORT).show();
//                                        dataExists = true;
////                                            break;
//                                    } else {
//                                        dataExists = false;
//                                    }
////                                    }
//
//                                    if (!dataExists) {
//                                        // Store user data
//                                        databaseReference.child(finalChildPath).setValue(dtoJsonEntity)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void unused) {

    /// /                                                        Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                }).addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
//                                    }
//
//                                } else {
//                                    DtoJsonEntity dtoJsonEntity = new DtoJsonEntity(
//                                            finalInvoiceId,
//                                            dtoJson.getName(),
//                                            dtoJson.getTotal(),
//                                            dtoJson.getCreateddtm(),
//                                            dtoJson.getDate(),
//                                            itemListJson);
//                                    databaseReference.child(finalChildPath).setValue(dtoJsonEntity);
//
//                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });
//
//
//                    }
//                }
//
//
//                // Notify on the main thread
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "[Invoices] Restored " + counter + " rows", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//                try {
//                    reader.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//
//
//    }
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

        View csvViewLayout = getLayoutInflater().inflate(R.layout.csv_item_list, null, false);
        ListView csvListView = csvViewLayout.findViewById(R.id.csvListView);
        EditText etCsvItemName = csvViewLayout.findViewById(R.id.etCsvItemName);
        Button btnAddCsvItem = csvViewLayout.findViewById(R.id.btnAddCsvItem);

        etCsvItemName.requestFocus();

        CsvAdapter csvAdapter = new CsvAdapter(this, csvItemsList);
        csvListView.setAdapter(csvAdapter);

        btnAddCsvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etCsvItemName != null && StringUtils.isNotEmpty(etCsvItemName.getText())
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
            Toast.makeText(this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

                if (etAddItem != null && !etAddItem.getText().toString().isEmpty()
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
        loadItemsFromFile(itemList, item);
        adapter.notifyDataSetChanged();
    }

    private void callReport() {
        // DO NOT REMOVE BELOW LINES  ***
        // ** Either Call below activity or enable showReportsDialog() method  //

//        Intent intent = new Intent(this, ReportActivity.class);
//        startActivity(intent);

        showReportsDialog();


    }

    private void showReportsDialog() {


        DatabaseHelper dbHelper;
        Button getBtn;
        TextView tvStartDate, tvEndDate;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reports");
        builder.setIcon(R.drawable.ic_report);
        final View customView = getLayoutInflater().inflate(R.layout.activity_report, null);


        dbHelper = new DatabaseHelper(this);
        getBtn = customView.findViewById(R.id.getBtn);
        tvStartDate = customView.findViewById(R.id.tvStartDate);
        tvEndDate = customView.findViewById(R.id.tvEndDate);
        Spinner spinnerSelectPeriod = customView.findViewById(R.id.selectPeriod);
        pd = new ProgressDialog(this);
        String[] items = {"select option", "Last month", "Last 3 months", "Last 6 months", "Last 1 year", "Custom"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter.getView(0, null, null).setEnabled(false);

        spinnerSelectPeriod.setAdapter(adapter);

        spinnerSelectPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getSelectedItem().equals("Custom")) {
                    tvStartDate.setVisibility(View.VISIBLE);
                    tvEndDate.setVisibility(View.VISIBLE);
                    tvStartDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateTimeDialog();
                        }

                        private void showDateTimeDialog() {
                            final Calendar currentDate = Calendar.getInstance();
                            final Calendar date = Calendar.getInstance();

                            new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    date.set(year, month, dayOfMonth);
                                    tvStartDate.setText(DateFormat.format("dd-MM-yyyy", date));
                                }
                            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
                        }
                    });

                    tvEndDate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateTimeDialog();
                        }

                        private void showDateTimeDialog() {
                            final Calendar currentDate = Calendar.getInstance();
                            final Calendar date = Calendar.getInstance();

                            new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    date.set(year, month, dayOfMonth);
                                    tvEndDate.setText(DateFormat.format("dd-MM-yyyy", date));

                                }
                            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
                        }
                    });

                } else {
                    tvStartDate.setVisibility(View.GONE);
                    tvEndDate.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        getBtn.setOnClickListener(new View.OnClickListener() {
            private int reportsTotal = 0;

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String selecteditem = (String) spinnerSelectPeriod.getSelectedItem();
                if (selecteditem.equalsIgnoreCase("Last month")) {
                    processDateRangeGenerateReport(30);
                } else if (selecteditem.equalsIgnoreCase("Last 3 months")) {
                    processDateRangeGenerateReport(90);
                } else if (selecteditem.equalsIgnoreCase("Last 6 months")) {
                    processDateRangeGenerateReport(180);
                } else if (selecteditem.equalsIgnoreCase("Last 1 year")) {
                    processDateRangeGenerateReport(365);
                } else if (selecteditem.equalsIgnoreCase("Custom")) {

                    if (tvStartDate != null && !tvStartDate.getText().equals("Start Date Here")
                            && tvEndDate != null && !tvEndDate.getText().equals("End Date Here")) {
                        String startDateFormatted = getParsedDate(tvStartDate.getText().toString());
                        String endDateFormatted = getParsedDate(tvEndDate.getText().toString());

                        //                    int total = 0;
                        Cursor cursor = dbHelper.getPeriodRecords(startDateFormatted, endDateFormatted);

                        Map<String, Integer> map = new HashMap<>();
                        if (cursor.getCount() > 0) {
                            while (cursor.moveToNext()) {
                                double itemVal = 0.0d;
                                String jsonItemList = cursor.getString(1);
                                reportsTotal += cursor.getInt(2);
                                List<CustomItem> itemList = getParserJsonList(jsonItemList);
                                for (CustomItem customItem : itemList) {
                                    if (map.containsKey(customItem.getName())) {
                                        itemVal = map.get(customItem.getName()) + (int) customItem.getSliderValue();
                                    } else {
                                        itemVal = (int) customItem.getSliderValue();
                                    }
                                    map.put(customItem.getName(), (int) itemVal);
                                    itemVal = 0.0d;
                                }
                            }
                            System.err.println("map: " + map);
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            builder1.setCancelable(false);
                            builder1.setTitle("Color or B&W pdf ? ");
                            builder1.setPositiveButton("Color", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    generateReportPdf(startDateFormatted, endDateFormatted, map, reportsTotal, true);
                                    reportsTotal = 0;
                                }
                            });
                            builder1.setNegativeButton("B&W", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    generateReportPdf(startDateFormatted, endDateFormatted, map, reportsTotal, false);
                                    reportsTotal = 0;
                                }
                            });

                            AlertDialog dialog1 = builder1.create();
                            dialog1.show();
                        } else {
                            View rootView = findViewById(android.R.id.content);
                            Snackbar.make(rootView, "No data to show !", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    })
                                    .show();
                        }
                    }
                }
            }

            private String getParsedDate(String dateStr) {
                SimpleDateFormat dateFormatSource = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat dateFormatTarget = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date parsedDate = dateFormatSource.parse(dateStr);
                    String formattedDate = dateFormatTarget.format(parsedDate);
                    return formattedDate;
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        builder.setView(customView);


        AlertDialog dialog = builder.create();
        dialog.show();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateReportPdf(String oldDateVal, String newDateVal, Map<String, Integer> map, int total, boolean colorFul) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            String pdfPath = pdfPathMain + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "reports";

            if (!Files.exists(Paths.get(pdfPath))) {
                new File(pdfPath).mkdirs();
            }

            LocalDateTime localDateTime = LocalDateTime.now();
            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            File pdfFile = new File(pdfPath, "invoice_report_" + frmtted + ".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add title
            Paragraph title = new Paragraph("Invoice Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold();
            document.add(title);

            // Load the image from assets
            AssetManager assetManager = getApplicationContext().getAssets();
            InputStream inputStream = assetManager.open("analytics (1).png");
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);
            image.setWidth(30);
            image.setHeight(30);
            document.add(image);

            // Add customer details
            Paragraph customerDetails = new Paragraph()
                    .add("From  " + getFormattedDate(oldDateVal) + " to " + getFormattedDate(newDateVal) + "\n")
                    .add("Total sales in (Rs) : " + total + "\n\n");
            document.add(customerDetails);

            // Add table for items
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
                    .useAllAvailableWidth();

            table.setTextAlignment(TextAlignment.CENTER);

            if (colorFul) {
                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Item Description")).setBackgroundColor(headerColor));
                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Rate")).setBackgroundColor(headerColor));
                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Quantity")).setBackgroundColor(headerColor));
                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Amount")).setBackgroundColor(headerColor));
            } else {
                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Item Description")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Rate")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Quantity")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Amount")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }

            if (map != null) {
                int index = 0;
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    boolean isOdd = index++ % 2 == 1;
                    if (colorFul) {
                        if (isOdd) {
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(entry.getKey()))));
                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
                            Integer qty = entry.getValue();
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price))));
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(qty))));
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price * qty))));
                        } else {
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(entry.getKey()))));
                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
                            Integer qty = entry.getValue();
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price))));
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(qty))));
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price * qty))));
                        }
                    } else {
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getKey()))));
                        Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
                        Integer qty = entry.getValue();
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price))));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(qty))));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price * qty))));
                    }
                }
            }

            // ================== UPDATED FOOTER ==================
            // Calculate total quantity
            int totalQuantity = 0;
            if (map != null) {
                for (int qty : map.values()) {
                    totalQuantity += qty;
                }
            }

            if (colorFul) {
                Cell emptyCell = new Cell().setBorder(null).setBackgroundColor(headerColor).add(new Paragraph(""));
                Cell totalLabelCell = new Cell().setBorder(null).setBold().setBackgroundColor(headerColor)
                        .add(new Paragraph("Total")).setTextAlignment(TextAlignment.CENTER);
                Cell totalQtyCell = new Cell().setBorder(null).setBold().setBackgroundColor(headerColor)
                        .add(new Paragraph(String.valueOf(totalQuantity))).setTextAlignment(TextAlignment.CENTER);
                Cell totalAmtCell = new Cell().setBorder(null).setBold().setBackgroundColor(headerColor)
                        .add(new Paragraph(String.valueOf(total))).setTextAlignment(TextAlignment.CENTER);

                table.addFooterCell(emptyCell);
                table.addFooterCell(totalLabelCell);
                table.addFooterCell(totalQtyCell);
                table.addFooterCell(totalAmtCell);
            } else {
                Cell emptyCell = new Cell().setBackgroundColor(ColorConstants.LIGHT_GRAY).add(new Paragraph(""));
                Cell totalLabelCell = new Cell().setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .add(new Paragraph("Total")).setTextAlignment(TextAlignment.CENTER);
                Cell totalQtyCell = new Cell().setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .add(new Paragraph(String.valueOf(totalQuantity))).setTextAlignment(TextAlignment.CENTER);
                Cell totalAmtCell = new Cell().setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .add(new Paragraph(String.valueOf(total))).setTextAlignment(TextAlignment.CENTER);

                table.addFooterCell(emptyCell);
                table.addFooterCell(totalLabelCell);
                table.addFooterCell(totalQtyCell);
                table.addFooterCell(totalAmtCell);
            }
            // ================== END OF UPDATED FOOTER ==================

            document.add(table);

            Paragraph footerDate = new Paragraph()
                    .add("\nReport generated on " + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date()) + "\n");
            document.add(footerDate);

            document.close();
            writer.close();
            openGeneratedPDF(pdfFile);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    Old method (Working)
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void generateReportPdf(String oldDateVal, String newDateVal, Map<String, Integer> map, int total, boolean colorFul) {
//        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
//
//        try {
//            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//            String pdfPath = pdfPathMain + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "reports";
//
//            if (!Files.exists(Paths.get(pdfPath))) {
//                new File(pdfPath).mkdirs();
//            }
//
//            LocalDateTime localDateTime = LocalDateTime.now();
//            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//
//            File pdfFile = new File(pdfPath, "invoice_report_" + frmtted + ".pdf");
//            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
//            PdfDocument pdfDocument = new PdfDocument(writer);
//            Document document = new Document(pdfDocument);
//
//
//            // Add title
//            Paragraph title = new Paragraph("Invoice Report")
//                    .setTextAlignment(TextAlignment.CENTER)
//                    .setFontSize(20)
//                    .setBold();
//            document.add(title);
//
//            // Load the image from assets
//            AssetManager assetManager = getApplicationContext().getAssets();
//            InputStream inputStream = assetManager.open("analytics (1).png");
//            byte[] imageBytes = new byte[inputStream.available()];
//            inputStream.read(imageBytes);
//            inputStream.close();
//
//            ImageData imageData = ImageDataFactory.create(imageBytes);
//            Image image = new Image(imageData);
//            image.setWidth(30);
//            image.setHeight(30);
//            // Add the image to the document
//            document.add(image);
//
//
//            // Add customer details
//            Paragraph customerDetails = new Paragraph()
//                    .add("From  " + getFormattedDate(oldDateVal) + " to " + getFormattedDate(newDateVal) + "\n")
//                    .add("Total sales in (Rs) : " + total + "\n\n");
//            document.add(customerDetails);
//
//            // Add table for items
//            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
//                    .useAllAvailableWidth();
//
//            table.setTextAlignment(TextAlignment.CENTER);
//
//
//            if (colorFul) {
//                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Item Description")).setBackgroundColor(headerColor));
//                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Rate")).setBackgroundColor(headerColor));
//                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Quantity")).setBackgroundColor(headerColor));
//                table.addHeaderCell(new Cell().setBorder(null).setBold().add(new Paragraph("Amount")).setBackgroundColor(headerColor));
//
//            } else {
//                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Item Description")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Rate")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Quantity")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//                table.addHeaderCell(new Cell().setBold().add(new Paragraph("Amount")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//            }
//
//
//            if (map != null) {
//                int index = 0;
//
//                for (Map.Entry<String, Integer> entry : map.entrySet()) {
//                    boolean isOdd = index++ % 2 == 1;
//                    if (colorFul) {
//                        if (isOdd) {
//                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(entry.getKey()))));
//                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
//                            Integer qty = entry.getValue();
//                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price))));
//                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(qty))));
//                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price * qty))));
//                        } else {
//                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(entry.getKey()))));
//                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
//                            Integer qty = entry.getValue();
//                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price))));
//                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(qty))));
//                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price * qty))));
//                        }
//                    } else {
//                        table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getKey()))));
//                        Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()), 0);
//                        Integer qty = entry.getValue();
//                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price))));
//                        table.addCell(new Cell().add(new Paragraph(String.valueOf(qty))));
//                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price * qty))));
//                    }
//                }
//            }
//
//            if (colorFul) {
//                table.addFooterCell(new Cell(1, 2).setBorder(null).setBold().add(new Paragraph("")));
//                table.addFooterCell(new Cell().setBorder(null).setBold().add(new Paragraph("Total")).setBackgroundColor(headerColor));
//                table.addFooterCell(new Cell().setBorder(null).setBold().add(new Paragraph(String.valueOf(total))).setBackgroundColor(headerColor));
//            } else {
//                table.addFooterCell(new Cell(1, 2).setBorder(null).setBold().add(new Paragraph("")));
//                table.addFooterCell(new Cell().setBold().add(new Paragraph("Total")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//                table.addFooterCell(new Cell().setBold().add(new Paragraph(String.valueOf(total))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
//            }
//
//
//            document.add(table);
//
//            Paragraph footerDate = new Paragraph()
//                    .add("\nReport generated on " + new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date()) + "\n");
//
//            document.add(footerDate);
//
//

    /// /            // Add total
    /// /            Paragraph total = new Paragraph("Total: " + dtoJson.getTotal())
    /// /                    .setTextAlignment(TextAlignment.RIGHT)
    /// /                    .setFontSize(15)
    /// /                    .setBold();
    /// /            document.add(total);
//
//            document.close();
//            writer.close();
//            openGeneratedPDF(pdfFile);
//
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFormattedDate(String dateVal) {
        LocalDate localDateFmtted = LocalDate.parse(dateVal, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String month = localDateFmtted.format(DateTimeFormatter.ofPattern("MMM")).toLowerCase();

        String dateFmtted = localDateFmtted.getDayOfMonth() + "-" + month + "-" + localDateFmtted.getYear();
        return dateFmtted;
    }

    private List<CustomItem> getParserJsonList(String jsonItemList) {

        JsonObject jsonObject = (new JsonParser()).parse(jsonItemList).getAsJsonObject();
        JsonArray listArr = jsonObject.getAsJsonArray("itemList");
        JsonArray listOtherArr = jsonObject.getAsJsonArray("otherItemsList");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CustomItem>>() {
        }.getType();
        List<CustomItem> itemList = gson.fromJson(listArr, listType);
        List<CustomItem> otherItemList = gson.fromJson(listOtherArr, listType);
        itemList.addAll(otherItemList);
        return itemList;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processDateRangeGenerateReport(int noOfDays) {
        LocalDate date = LocalDate.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        calendar.add(Calendar.DAY_OF_MONTH, -noOfDays);
        Date newDate = calendar.getTime();


        String oldDateVal = new SimpleDateFormat("yyyy-MM-dd").format(newDate);
        String newDateVal = String.valueOf(date);


        Cursor cursor = dbHelper.getPeriodRecords(oldDateVal, newDateVal);
        double itemVal = 0.0d;
        Map<String, Integer> map = new HashMap<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                itemVal = 0.0d;
                String jsonItemList = cursor.getString(1);
                reportsTotal += cursor.getInt(2);
                List<CustomItem> itemList = getParserJsonList(jsonItemList);
                for (CustomItem customItem : itemList) {
                    itemVal += Double.valueOf(customItem.getSliderValue());

                    if (map.containsKey(customItem.getName())) {
                        int itemQty = map.get(customItem.getName());
                        map.put(customItem.getName(), itemQty + (int) itemVal);
                    } else {
                        map.put(customItem.getName(), (int) itemVal);
                    }
                    itemVal = 0.0d;
                }
            }

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setCancelable(false);
            builder1.setTitle("Color or B&W pdf ? ");
            builder1.setPositiveButton("Color", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    generateReportPdf(oldDateVal, newDateVal, map, reportsTotal, true);
                    reportsTotal = 0;
                }
            });
            builder1.setNegativeButton("B&W", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    generateReportPdf(oldDateVal, newDateVal, map, reportsTotal, false);
                    reportsTotal = 0;
                }
            });

            AlertDialog dialog1 = builder1.create();
            dialog1.show();


        } else {
            View rootView = findViewById(android.R.id.content);
            Snackbar.make(rootView, "No data to show !", Snackbar.LENGTH_LONG)
                    .setDuration(5000)
                    .setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    })
                    .show();
        }
    }

    private void callTest() {
        Intent intent = new Intent(MainActivity.this, SharedPrefActivity.class);
        intent.putExtra("sharedPrefList", (Serializable) itemList);
        startActivity(intent);
    }

    private void deleteEntireCloudData() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(false);

        builder.setTitle("Delete all the cloud data?");
        builder.setMessage("Are you sure you want to delete all the cloud data?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.setMessage("Please wait");
                pd.show();
//                dbHelper.deleteFireStoreData(MainActivity.this,pd);

                SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
                String deviceModel = sharedPreferences.getString("model", Build.MODEL);

                // Reference to the Firebase Realtime Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = database.getReference(deviceModel + "/" + "invoices");
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

                database.getReference(deviceModel + "/" + "expenses").child("/").removeValue()
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
        String invoicesFilePath = path + File.separator + "InvoicesBackup.csv";
        String expensesFilePath = path + File.separator + "ExpensesBackup.csv";

        File invoicesFile = new File(invoicesFilePath);
        File expensesFile = new File(expensesFilePath);

        ArrayList<Uri> fileUris = new ArrayList<>();

        // Add InvoicesBackup.csv if it exists
        if (invoicesFile.exists()) {
            Uri invoicesUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", invoicesFile);
            fileUris.add(invoicesUri);
        }

        // Add ExpensesBackup.csv if it exists
        if (expensesFile.exists()) {
            Uri expensesUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", expensesFile);
            fileUris.add(expensesUri);
        }

        // If no files exist, show a toast and return
        if (fileUris.isEmpty()) {
            Toast.makeText(this, "No backup files found to attach", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/csv");  // MIME type for CSV files
        emailIntent.putExtra(Intent.EXTRA_EMAIL, InvoiceConstants.EMAIL_RECIPIENTS);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice Manager\nBackup Files " + new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date()));
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant permission to read URIs
//        emailIntent.putExtra(Intent.EXTRA_TEXT, "Invoice manager backup files");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (ActivityNotFoundException ex) {
            Log.e("MainActivity", "No email client found", ex);
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }

    }


//    private void sendEmailWithAttachment() {
//
//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
//        String filePath = path + File.separator + "InvoicesBackup.csv";
//
//        File file = new File(filePath);
//
//        Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
//
//        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        emailIntent.setType("text/csv");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, InvoiceConstants.EMAIL_RECIPIENTS);
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoices Backup " + new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date()));

    /// /        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email body text");
//
//
//        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
//
//        try {
//            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
//        } catch (ActivityNotFoundException ex) {
//            Log.e("MainActivity", "No email client found", ex);
//        }
//
//
//    }
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
        toast.setGravity(Gravity.TOP | Gravity.CENTER, toast.getXOffset(), toast.getYOffset());
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suggestionHelper != null) {
            suggestionHelper.close();
        }
    }

    private class LoadSuggestionsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            // Insert default suggestions if table is empty
            suggestionHelper.insertDefaultSuggestionsIfNeeded();

            // Get all suggestions from database
            return suggestionHelper.getAllSuggestions();
        }

        @Override
        protected void onPostExecute(List<String> suggestions) {
            // Update adapter with suggestions
            suggestionAdapter.clear();
            suggestionAdapter.addAll(suggestions);
            suggestionAdapter.notifyDataSetChanged();

            StringBuilder stringBuilder = new StringBuilder();

            suggestions.forEach(s -> stringBuilder.append(s).append("\n"));


            Log.i("Sugg", "" + stringBuilder);

        }

        // Method to add new suggestion (you can call this from a button or other event)
        private void addNewSuggestion(String newSuggestion) {
            new AsyncTask<String, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(String... suggestions) {
                    long result = suggestionHelper.insertSuggestion(suggestions[0]);
                    return result != -1; // -1 means insertion failed (duplicate)
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    if (success) {
                        // Reload suggestions
                        new LoadSuggestionsTask().execute();
                    }
                }
            }.execute(newSuggestion);
        }

    }
}