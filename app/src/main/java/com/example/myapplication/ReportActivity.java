package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.adapterholders.CustomItem;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    Button getBtn;
    TextView tvStartDate,tvEndDate;
    private static final String SHARED_PREFS_FILE = "my_shared_prefs";

    Color headerColor1 = new DeviceRgb(102, 178, 255); //
    Color headerColor = new DeviceRgb(255, 128, 0); // Orange
    Color oddRowColor1 = new DeviceRgb(204, 229, 255); //
    Color oddRowColor = new DeviceRgb(255, 229, 204); // Light Orange
    ProgressDialog pd;

    int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        dbHelper = new DatabaseHelper(this);
        getBtn = findViewById(R.id.getBtn);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        Spinner spinnerSelectPeriod = findViewById(R.id.selectPeriod);
        pd = new ProgressDialog(this);
        String[] items = {"select option","Last month", "Last 3 months", "Last 6 months", "Last 1 year", "Custom"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter.getView(0, null, null).setEnabled(false);

        spinnerSelectPeriod.setAdapter(adapter);

        spinnerSelectPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getSelectedItem().equals("Custom")) {
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

                            new DatePickerDialog(ReportActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    date.set(year, month, dayOfMonth);
                                    tvStartDate.setText(android.text.format.DateFormat.format("dd-MM-yyyy", date));
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

                            new DatePickerDialog(ReportActivity.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                    date.set(year, month, dayOfMonth);
                                    tvEndDate.setText(android.text.format.DateFormat.format("dd-MM-yyyy", date));

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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String selecteditem = (String) spinnerSelectPeriod.getSelectedItem();
                if(selecteditem.equalsIgnoreCase("Last month")){
                    processDateRangeGenerateReport(30);
                } else if (selecteditem.equalsIgnoreCase("Last 3 months")){
                    processDateRangeGenerateReport(90);
                } else if (selecteditem.equalsIgnoreCase("Last 6 months")){
                    processDateRangeGenerateReport(180);
                } else if (selecteditem.equalsIgnoreCase("Last 1 year")){
                    processDateRangeGenerateReport(365);
                } else if (selecteditem.equalsIgnoreCase("Custom")){

                    if(tvStartDate != null && !tvStartDate.getText().equals("Start Date Here")
                            && tvEndDate != null && !tvEndDate.getText().equals("End Date Here")) {



                    String startDateFormatted = getParsedDate(tvStartDate.getText().toString());
                    String endDateFormatted = getParsedDate(tvEndDate.getText().toString());

//                    int total = 0;
                    Cursor cursor = dbHelper.getPeriodRecords(startDateFormatted,endDateFormatted);
                    double itemVal=0.0d;
                    Map<String, Integer> map = new HashMap<>();
                    if(cursor.getCount()>0){
                        while(cursor.moveToNext()){
                            String jsonItemList = cursor.getString(1);
                            total += cursor.getInt(2);
                            List<CustomItem> itemList = getParserJsonList(jsonItemList);
                            for (CustomItem customItem : itemList) {
                                itemVal += Double.valueOf(customItem.getSliderValue());
                                map.put(customItem.getName(), (int) itemVal);
                            }
                        }
                    }

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ReportActivity.this);
                        builder1.setCancelable(false);
                        builder1.setTitle("Color or B&W pdf ? ");
                        builder1.setPositiveButton("Color", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                generateReportPdf(startDateFormatted,endDateFormatted, map, total, true);
                            }
                        });
                        builder1.setNegativeButton("B&W", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                generateReportPdf(startDateFormatted,endDateFormatted, map, total, false);
                            }
                        });

                        AlertDialog dialog1 = builder1.create();
                        dialog1.show();



                        AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("View Barchart ?");
                        builder.setPositiveButton("Show", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ReportActivity.this, BarChartActivity.class);
                                intent.putExtra("map", (Serializable) map);
                                startActivity(intent);
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

//                    writeToFile(startDateFormatted,endDateFormatted, map, total, 0);

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

    }

    private String printMap(Map<String, Integer> map) {

        StringBuilder stringBuilder = new StringBuilder();

        map.forEach((k,v) -> {
            stringBuilder.append(k);
            stringBuilder.append(" : ");
            stringBuilder.append(map.get(k));
            stringBuilder.append("\n");
        });

        return stringBuilder.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeToFile(String oldDateVal, String newDateVal, Map<String, Integer> map, int total, int noOfDays) {
        String pathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        String subPath = pathMain + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "reports";


        if(!Files.exists(Paths.get(subPath))) {
            new File(subPath).mkdirs();
        }
        String filePath = subPath + File.separator + "report_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".txt";

        FileOutputStream fos = null;
        try {
            String content = "Last "+noOfDays+" days report details\nFrom "+oldDateVal+" - To "+newDateVal+"\n\nItems ~~ Qty\n\n"+printMap(map)+" \n\nTotal Sales in (Rs): "+total;
            fos = new FileOutputStream(new File(filePath));
            fos.write(content.getBytes());
            Toast.makeText(ReportActivity.this, "Report generated in documents", Toast.LENGTH_LONG).show();
            File file = new File(filePath);
            file.setReadable(true, false);
//            openGeneratedFile(file);
        } catch (IOException e) {
            Toast.makeText(ReportActivity.this, "Report generation failed", Toast.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e("MainActivity", "Error closing file: " + e.toString());
                }
            }
        }

    }

    private void openGeneratedFile(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "text/plain");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(intent, "Open File");
        try {
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to open PDF", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processDateRangeGenerateReport(int noOfDays) {
        LocalDate date = LocalDate.now();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonthValue()-1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        calendar.add(Calendar.DAY_OF_MONTH, -noOfDays);
        Date newDate = calendar.getTime();


        String oldDateVal = new SimpleDateFormat("yyyy-MM-dd").format(newDate);
        String newDateVal = String.valueOf(date);




        Cursor cursor = dbHelper.getPeriodRecords(oldDateVal,newDateVal);
        double itemVal=0.0d;
        Map<String, Integer> map = new HashMap<>();
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                itemVal = 0.0d;
                String jsonItemList = cursor.getString(1);
                total += cursor.getInt(2);
                List<CustomItem> itemList = getParserJsonList(jsonItemList);
                for (CustomItem customItem : itemList) {
                    itemVal += Double.valueOf(customItem.getSliderValue());
                    map.put(customItem.getName(), (int) itemVal);
                }
            }
        }

//        writeToFile(oldDateVal,newDateVal, map, total, noOfDays);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setCancelable(false);
        builder1.setTitle("Color or B&W pdf ? ");
        builder1.setPositiveButton("Color", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                generateReportPdf(oldDateVal,newDateVal, map, total, true);
            }
        });
        builder1.setNegativeButton("B&W", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                generateReportPdf(oldDateVal,newDateVal, map, total, false);
            }
        });

        AlertDialog dialog1 = builder1.create();
        dialog1.show();



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("View Barchart ?");
        builder.setPositiveButton("Show", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ReportActivity.this, BarChartActivity.class);
                intent.putExtra("map", (Serializable) map);
                startActivity(intent);
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
            // Add the image to the document
            document.add(image);


            // Add customer details
            Paragraph customerDetails = new Paragraph()
                    .add("From  " + oldDateVal + " to " + newDateVal + "\n")
                    .add("Total sales in (Rs) : " + total + "\n\n");
            document.add(customerDetails);

            // Add table for items
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1}))
                    .useAllAvailableWidth();

            table.setTextAlignment(TextAlignment.CENTER);


            if(colorFul){
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
                    if(colorFul) {
                        if(isOdd){
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(entry.getKey()))));
                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()),0);
                            Integer qty = entry.getValue();
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price))));
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(qty))));
                            table.addCell(new Cell().setBorder(null).setBackgroundColor(oddRowColor).add(new Paragraph(String.valueOf(price * qty))));
                        } else {
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(entry.getKey()))));
                            Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()),0);
                            Integer qty = entry.getValue();
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price))));
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(qty))));
                            table.addCell(new Cell().setBorder(null).add(new Paragraph(String.valueOf(price * qty))));
                        }
                    } else {
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getKey()))));
                        Integer price = sharedPreferences.getInt(entry.getKey().toString().toUpperCase(Locale.getDefault()),0);
                        Integer qty = entry.getValue();
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price))));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(qty))));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(price * qty))));
                    }
                }
            }

            if(colorFul){
                table.addFooterCell(new Cell(1,2).setBorder(null).setBold().add(new Paragraph("")));
                table.addFooterCell(new Cell().setBorder(null).setBold().add(new Paragraph("Total")).setBackgroundColor(headerColor));
                table.addFooterCell(new Cell().setBorder(null).setBold().add(new Paragraph(String.valueOf(total))).setBackgroundColor(headerColor));
            } else {
                table.addFooterCell(new Cell(1,2).setBorder(null).setBold().add(new Paragraph("")));
                table.addFooterCell(new Cell().setBold().add(new Paragraph("Total")).setBackgroundColor(ColorConstants.LIGHT_GRAY));
                table.addFooterCell(new Cell().setBold().add(new Paragraph(String.valueOf(total))).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }


            document.add(table);

            Paragraph footerDate = new Paragraph()
                    .add("\nReport generated on "+ new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a").format(new Date()) +"\n");

            document.add(footerDate);


//            // Add total
//            Paragraph total = new Paragraph("Total: " + dtoJson.getTotal())
//                    .setTextAlignment(TextAlignment.RIGHT)
//                    .setFontSize(15)
//                    .setBold();
//            document.add(total);

            document.close();
            writer.close();
            openGeneratedPDF(pdfFile);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<CustomItem> getParserJsonList(String jsonItemList) {

        JsonObject jsonObject = (new JsonParser()).parse(jsonItemList).getAsJsonObject();
        JsonArray listArr = jsonObject.getAsJsonArray("itemList");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CustomItem>>() {}.getType();
        List<CustomItem> itemList = gson.fromJson(listArr, listType);
        return itemList;
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
}

