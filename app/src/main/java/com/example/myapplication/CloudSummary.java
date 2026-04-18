package com.example.myapplication;

import static com.example.myapplication.constants.InvoiceConstants.MONTHLY_REPORTS_EXPORT_FOLDER_PATH;
import static com.example.myapplication.constants.InvoiceConstants.MONTHLY_SALES_REPORT_FILENAME;
import static com.example.myapplication.constants.InvoiceConstants.PDF_EXTENSION;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapters.Expense;
import com.example.myapplication.adapters.MainAdapter;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.dtos.Day;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.dtos.Month;
import com.example.myapplication.dtos.Year;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.exception.ContextedException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CloudSummary extends BaseActivity {
    DatabaseReference databaseReference;
    DatabaseReference usersRef;
    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    static CopyOnWriteArrayList<Month> monthList;

    public static Map<String, List<DtoJsonEntity>> map = new HashMap<>();
    public static Map<String, Map<String, List<DtoJsonEntity>>> monthMap = new HashMap<>();
    public static Map<String, Map<String, Map<String, List<DtoJsonEntity>>>> yearMap = new HashMap<>();
    private MainAdapter mainAdapter;
    private static AppCompatSpinner yearSpinner;
    private CopyOnWriteArrayList<Year> yearList;
    private TextView yearTotal;
    private static TextView noDataCloudTextView;
    private static ImageView noDataCloudImageView,yearlySalesBtn;
    private Map<String, Integer> amountMap= new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // applyUserTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_summary);
        recyclerView = findViewById(R.id.recyclerview);
        yearSpinner = findViewById(R.id.yearSpinner);
        yearTotal = findViewById(R.id.yearTotal);
        noDataCloudTextView = findViewById(R.id.noDataCloudTextView);
        noDataCloudImageView = findViewById(R.id.noDataCloudImageView);
        yearlySalesBtn = findViewById(R.id.yearlySalesBtn);
        progressDialog = new ProgressDialog(this);
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        monthList = new CopyOnWriteArrayList<>();

        loadSpinner(yearSpinner);

        fetchFromFirebase(map);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString();
                fetchFromFirebase(map);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });


        yearlySalesBtn.setOnClickListener(v -> {
            progressDialog.setMessage("Generating Yearly Report");
            progressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<DtoJsonEntity> finalList = new ArrayList<>();
                    yearList.forEach(year -> {
                        year.getMonthList().forEach(month -> {
                            month.getDayList().forEach(day -> {
                                finalList.addAll(day.getDtoJsonEntityList());
                            });
                        });
                    });

                    Map<String, Integer> mapResovled = parseJsonAndCalculateYearly(finalList);

                    mapResovled.forEach((k, val) -> {
                        amountGrandtotal += (InvoiceConstants.ITEM_PRICE_MAP.containsKey(k.toUpperCase(Locale.ENGLISH)) ?
                                val * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(k.toUpperCase(Locale.ENGLISH),0) :
                                amountMap.getOrDefault(k.toUpperCase(Locale.ENGLISH), 0));
                        System.err.println(k+" "+amountGrandtotal);
                    });

                    File pdfFile = generateMonthlyReport(CloudSummary.this, mapResovled, amountMap, yearList.get(0).getYearName(), amountGrandtotal);

                    // Update UI on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (pdfFile != null) {
                                showSnackbarWithOpenActionYearly(pdfFile, CloudSummary.this);
                            }
                        }
                    });


                    amountGrandtotal=0;
                    mapResovled.clear();
                    amountMap.clear();
                }
            }).start();
        });

    }

    // private void applyUserTheme() { ... } removed


    private void loadSpinner(AppCompatSpinner yearSpinner) {
        // Get the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Generate a list of years (e.g., from 1900 to current year)
        List<String> years = new ArrayList<>();
        for (int year = 2022; year <= currentYear + 3; year++) {
            years.add(String.valueOf(year));
        }

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );

        // Set the layout for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the Spinner
        yearSpinner.setAdapter(adapter);

        // Optionally, set the current year as the selected item
        yearSpinner.setSelection(adapter.getPosition(String.valueOf(currentYear)));
    }
    private Integer amountGrandtotal=0;

    private void fetchFromFirebase(Map<String, List<DtoJsonEntity>> map) {

        progressDialog.setMessage("Fetching from cloud database");
        progressDialog.show();

        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
        String deviceModel = sharedPreferences.getString("model", Build.MODEL);

        databaseReference = FirebaseDatabase.getInstance().getReference(deviceModel + "/" + "invoices");
        databaseReference.addValueEventListener(new ValueEventListener() {
            private String key;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                Object data = dataSnapshot.getValue();
                Gson gson = null;

                boolean enableCloudExportVal = sharedPreferences.getBoolean("enableCloudExport", false);

                if (enableCloudExportVal) {
                    boolean isPrettyPrint = sharedPreferences.getBoolean("enablePrettyPrintExport", false);
                    if (isPrettyPrint)
                        gson = new GsonBuilder().setPrettyPrinting().create();
                    else
                        gson = new GsonBuilder().create();
                    String json = gson.toJson(data);

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(dataSnapshot.getKey(), new JSONObject(json));
                        String finalOutput = null;
                        if (isPrettyPrint)
                            finalOutput = jsonObject.toString(4);
                        else
                            finalOutput = jsonObject.toString();
                        exportJsonToFile(finalOutput, InvoiceConstants.CLOUD_EXPORT_JSON_FILE_NAME);


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }


                Long expAmt = 0L;
                Long expAmtMonth = 0L;
                yearList = new CopyOnWriteArrayList<>();
                monthList = new CopyOnWriteArrayList<>();
                for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {

                    Year year = new Year();

                    for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                        expAmtMonth = 0L;
                        List<Day> dayList = new ArrayList<>();
                        Month month = new Month();


                        for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                            Day day = new Day();
                            key = daySnapshot.getKey();
                            expAmt = 0L;
                            List<DtoJsonEntity> tList = new ArrayList<>();
                            for (DataSnapshot subDay : daySnapshot.getChildren()) {
                                String userId = subDay.getKey();
                                DtoJsonEntity dtoJsonEntity = subDay.getValue(DtoJsonEntity.class);
                                expAmt += dtoJsonEntity.getTotal();
                                tList.add(dtoJsonEntity);

                            }
//                            nestedItems1.add(new NestedItem(d2.getKey(), "\u20B9"+expAmt, String.format("(%.2f%%)", ((double) expAmt/2000)*100),expAmt));
                            expAmtMonth += expAmt;
                            map.put(key, tList);
                            day.setDayName(key);
                            day.setDayTotal(expAmt);
                            day.setDtoJsonEntityList(tList);
                            dayList.add(day);
                        }
                        monthMap.put(monthSnapshot.getKey(), map);
                        month.setMonthName(monthSnapshot.getKey());
                        month.setMonthTotal(expAmtMonth);
                        month.setDayList(dayList);
                        month.setExpanded(false);
                        monthList.add(month);

                    }
                    yearMap.put(yearSnapshot.getKey(), monthMap);
                    year.setYearName(yearSnapshot.getKey());
                    year.setMonthList(monthList);
                    yearList.add(year);
                }

                String spinnerMonth = yearSpinner.getSelectedItem().toString();

                for (Month y : monthList) {
                    if (!y.getMonthName().contains(spinnerMonth)) {
                        monthList.remove(y);
                    }
                }
                int totalYear = monthList.stream().map(month -> month.getMonthTotal()).mapToInt(n -> n.intValue()).sum();
                yearTotal.setText("Total  \u20B9" + totalYear);


                // Set the adapter
                mainAdapter = new MainAdapter(CloudSummary.this, monthList);
                recyclerView.setAdapter(mainAdapter);

                List<DtoJsonEntity> finalList = new ArrayList<>();




            }

            private void exportJsonToFile(String finalOutput, String fileName) {
                try {
                    File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/InvMgrCloudJsonExport");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyy_HHmmss");
                    fileName = fileName + "_" + simpleDateFormat.format(new Date())+".json";
                    File file = new File(exportDir, fileName);

                    if (!exportDir.exists()) {
                        exportDir.mkdirs();
                    }

                    FileWriter writer = new FileWriter(file);
                    writer.write(finalOutput);
                    writer.close();
                } catch (IOException e) {
                    Toast.makeText(CloudSummary.this, "Cloud Export Failed!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                System.err.println("Error: " + databaseError.getMessage());
            }
        });
    }

    private void showSnackbarWithOpenActionYearly(File pdfFile, Context context) {
        View rootView = findViewById(android.R.id.content);

        Snackbar snackbar = Snackbar.make(rootView, "PDF generated successfully!", Snackbar.LENGTH_LONG);
        snackbar.setAction("Open", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdf(pdfFile);
            }
        });
        snackbar.show();
    }

    private void openPdf(File pdfFile) {
        // Create intent
        Context context = CloudSummary.this;
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            context.startActivity(Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            Toast.makeText(context, "No application available to open PDF", Toast.LENGTH_SHORT).show();
        }

    }

    private File generateMonthlyReport(Context context, Map<String, Integer> mainItemList, Map<String, Integer> amountMap, String yearName, Integer amountGrandtotal) {

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint totalPaint = new Paint();

        // Page dimensions
        int pageWidth = 300;
        int pageHeight = 600;

        // Margins and positions
        int leftMargin = 10;
        int rightMargin = 10;
        int topMargin = 40;
        int bottomMargin = 40;

        // Current Y position and line height
        int currentY = topMargin;
        int lineHeight = 20;

        // Table positions
        int productColX = 30;
        int quantityColX = 140;
        int amountColX = 220;

        // Start first page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page currentPage = pdfDocument.startPage(pageInfo);
        Canvas canvas = currentPage.getCanvas();

        // Title (only on first page)
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(16f);
        canvas.drawText("Yearly Sales Report ("+yearName+")", pageWidth / 2, currentY, titlePaint);
        currentY += 40;

        // Total (only on first page)
        totalPaint.setTextAlign(Paint.Align.LEFT);
        totalPaint.setColor(Color.BLACK);
        totalPaint.setTextSize(13f);
        canvas.drawText("Total \u20B9"+amountGrandtotal, 15, currentY, totalPaint);
        currentY += 40;

        // Table headers
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);
        paint.setTypeface(Typeface.MONOSPACE);
        canvas.drawText("Product", productColX, currentY, paint);
        canvas.drawText("Quantity", quantityColX, currentY, paint);
        canvas.drawText("Amount", amountColX, currentY, paint);
        currentY += 10;

        // Draw horizontal line under headers
        canvas.drawLine(leftMargin, currentY, pageWidth - rightMargin, currentY, paint);
        currentY += 20;

        Map<String, Integer> sortedMap = mainItemList.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Table rows
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            String product = entry.getKey();
            Integer quantity = entry.getValue();

            // Calculate amount
            int amount = InvoiceConstants.ITEM_PRICE_MAP.containsKey(product) ?
                    quantity * InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(product, 0) :
                    amountMap.getOrDefault(product, 0);

            // Check if we need a new page
            if (currentY + lineHeight > pageHeight - bottomMargin) {
                // Finish current page
                pdfDocument.finishPage(currentPage);

                // Start new page
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                currentPage = pdfDocument.startPage(pageInfo);
                canvas = currentPage.getCanvas();
                currentY = topMargin;

                // Add table headers on new page (optional)
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setColor(Color.BLACK);
                paint.setTextSize(12f);
                canvas.drawText("Product", productColX, currentY, paint);
                canvas.drawText("Quantity", quantityColX, currentY, paint);
                canvas.drawText("Amount", amountColX, currentY, paint);
                currentY += 10;
                canvas.drawLine(leftMargin, currentY, pageWidth - rightMargin, currentY, paint);
                currentY += 20;
            }

            // Draw table row
            canvas.drawText(product, productColX, currentY, paint);
            canvas.drawText(String.valueOf(quantity), quantityColX, currentY, paint);
            canvas.drawText(String.valueOf(amount), amountColX, currentY, paint);
            currentY += lineHeight;
        }

        // Finish the last page
        pdfDocument.finishPage(currentPage);

        // Save PDF to external storage
        File folder = new File(Environment.getExternalStorageDirectory()+"/"+Environment.DIRECTORY_DOCUMENTS + MONTHLY_REPORTS_EXPORT_FOLDER_PATH);

        if(!folder.exists()) {
            folder.mkdirs();
        }

        File pdfFile = new File(folder, MONTHLY_SALES_REPORT_FILENAME + yearName+System.currentTimeMillis() + PDF_EXTENSION);
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error while generating PDF!", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
        return pdfFile;

    }
    static int otherAmount = 0;

    private Map<String, Integer> parseJsonAndCalculateYearly(List<DtoJsonEntity> finalList) {
        int qty=0;
        int amount=0;

        Map<String, Integer> itemSaleMap = new HashMap<>();

        for (DtoJsonEntity item : finalList) {
            String itemJson = item.getItemListJsonStr();
            ObjectMapper objectMapper = new ObjectMapper();

            try {

                DtoJson itemObject = objectMapper.readValue(itemJson, DtoJson.class);

                List<CustomItem> items = itemObject.getItemList();
                List<CustomItem> otherItems = itemObject.getOtherItemsList();
                amount = 0;

                if(otherItems !=null && otherItems.size()>0) {
                    items.addAll(otherItems);
                }

                for (CustomItem i : items) {
                    if (itemSaleMap.containsKey(i.getName().toUpperCase(Locale.ENGLISH))) {
                        int tempQty = itemSaleMap.get(i.getName().toUpperCase(Locale.ENGLISH));
                        qty = (int) i.getSliderValue() + tempQty;
                        if(!InvoiceConstants.ITEM_PRICE_MAP.containsKey(i.getName().toUpperCase(Locale.ENGLISH))) {
                            otherAmount = otherAmount + i.getAmount();
                        } else {
                            amount = amount + i.getAmount();
                        }

                        itemSaleMap.put(i.getName().toUpperCase(Locale.ENGLISH), qty);
                        item.setName(i.getName());
                    } else {
                        amount = 0;
                        otherAmount = 0;
                        itemSaleMap.put(i.getName().toUpperCase(Locale.ENGLISH), (int) i.getSliderValue());
                        qty = (int) i.getSliderValue();
                        if(!InvoiceConstants.ITEM_PRICE_MAP.containsKey(i.getName().toUpperCase(Locale.ENGLISH))) {
                            otherAmount = otherAmount + i.getAmount();
                        } else {
                            amount = amount + i.getAmount();
                        }
                        item.setName(i.getName());
                        item.setQty(Long.valueOf(qty));

                    }
                    if(!InvoiceConstants.ITEM_PRICE_MAP.containsKey(i.getName().toUpperCase(Locale.ENGLISH))) {
                        int amt = amountMap.getOrDefault(i.getName().toUpperCase(Locale.ENGLISH),0);
                        int i1 = i.getAmount() + amt;
                        amountMap.put(i.getName().toUpperCase(Locale.ENGLISH), i1);
                    } else {
                        amountMap.put(i.getName().toUpperCase(Locale.ENGLISH), amount);
                    }
                }
                if(!InvoiceConstants.ITEM_PRICE_MAP.containsKey(item.getName().toUpperCase(Locale.ENGLISH))) {
                    item.setTotal(Long.valueOf(otherAmount));
                } else {

                    item.setTotal(Long.valueOf(amount));
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.err.println("map: "+ itemSaleMap);
        System.err.println("amountMap: "+ amountMap);
//        amountMap.clear();
        return itemSaleMap;
    }
}