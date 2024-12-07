package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.UserAdapter;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapterholders.DataModel;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.utils.PDFGeneratorUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
//    private TextView titleTextView;
//    private TextView subtitleTextView;
//    private TextView timestampTextView;
//    private TextView jsonTextView;
    private DatabaseHelper dbHelper;

    private static final String SHARED_PREFS_FILE = "my_shared_prefs";

    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        dbHelper = new DatabaseHelper(this);
        DataModel dataModel = (DataModel) getIntent().getSerializableExtra("dataModel");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        titleTextView = findViewById(R.id.titleTextView);
//        subtitleTextView = findViewById(R.id.subtitleTextView);
//        timestampTextView = findViewById(R.id.timestampTextView);
//        jsonTextView = findViewById(R.id.jsonTextView);




//            titleTextView.setText(dataModel.getTitle());
//            subtitleTextView.setText(dataModel.getSubtitle());
//            timestampTextView.setText(dataModel.getTimestamp());
//            jsonTextView.setText(dataModel.getProfileImageUrl());




            JsonObject jsonObject = (new JsonParser()).parse(dataModel.getProfileImageUrl())
                    .getAsJsonObject();

            JsonArray listArr = jsonObject.getAsJsonArray("itemList");
            JsonArray otherItemsListArr = jsonObject.getAsJsonArray("otherItemsList");

            Gson gson = new Gson();
            Type listType = new TypeToken<List<CustomItem>>() {}.getType();

            List<CustomItem> itemList = gson.fromJson(listArr, listType);
            List<CustomItem> otherItemsList = gson.fromJson(otherItemsListArr, listType);

            dataModel.setItemList(itemList);
            dataModel.setOtherItemsList(otherItemsList);
            List list = new ArrayList<>();
            list.add(dataModel);

            UserAdapter adapter = new UserAdapter(list, new UserAdapter.OnItemClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                public void onItemClick(String id, String date) {
                    Cursor cursor = dbHelper.getRecordById(dataModel.getId(),dataModel.getDate());
                    if(cursor.getCount()> 0){
                        while(cursor.moveToNext()){
                            String jsonStr = String.valueOf(cursor.getString(1));
                            JsonObject jsonObject1 = new JsonParser().parse(jsonStr).getAsJsonObject();
                            Gson gson1 = new Gson();
                            Type objectType = new TypeToken<DtoJson>() {}.getType();
                            DtoJson dtoJson = gson1.fromJson(jsonObject1, objectType);
                            File pdfFile = PDFGeneratorUtil.generateInvoice(dtoJson, cursor.getInt(0), getApplicationContext());
                            View rootView = findViewById(android.R.id.content);
                            Snackbar.make(rootView, "Invoice generated "+cursor.getInt(0), Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("View", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openGeneratedPDF(pdfFile);
                                            // Create an Intent to open the generated invoice
                                        }
                                    })
                                    .show();
                            Toast.makeText(DetailActivity.this, "File created in documents", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFormattedDateTime(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
        String formattedDate = dateTime.format(outputFormatter);
        return formattedDate;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generateInvoice(DtoJson dtoJson) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

            String pdfPath = pdfPathMain+File.separator+ InvoiceConstants.EMPLOYER_NAME +File.separator+"invoices";

            if(!Files.exists(Paths.get(pdfPath))){
                new File(pdfPath).mkdirs();
            }

            LocalDateTime localDateTime = LocalDateTime.now();
            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            File pdfFile = new File(pdfPath, "invoice_"+frmtted+".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add title
            Paragraph title = new Paragraph("Invoice")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setBold();
            document.add(title);


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
            openGeneratedPDF(pdfFile);
            // Create an Intent to open the generated invoice

            Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(this,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            }else {
                pendingIntent = PendingIntent.getActivity(this,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            }


            NotificationHelper.createNotificationChannel(DetailActivity.this);
            // Build the notification
            Notification.Builder builder = new Notification.Builder(DetailActivity.this, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_3d_rotation_24)
                    .setContentTitle("Invoice Generated")
                    .setContentText(pdfFile.getName())
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.baseline_document_scanner_24, "View", pendingIntent) // View action button
                    .setAutoCancel(true); // Auto cancel notification when clicked

            // Show the notification
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(1, builder.build()); // Use a unique notification ID for each notification
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

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