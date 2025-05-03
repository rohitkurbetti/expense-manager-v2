package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.dtos.DtoJson;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PDFGeneratorUtil {

    private static final String SHARED_PREFS_FILE = "my_shared_prefs";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static File generateInvoice(DtoJson dtoJson, long newRowId, Context context) {
        File pdfFile = null;
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            String pdfPathMain = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

            String pdfPath = pdfPathMain + File.separator + InvoiceConstants.EMPLOYER_NAME + File.separator + "invoices";

            if (!Files.exists(Paths.get(pdfPath))) {
                new File(pdfPath).mkdirs();
            }

            LocalDateTime localDateTime = LocalDateTime.now();
            String frmtted = localDateTime.format(DateTimeFormatter.ofPattern("ddMMMyy_HHmmss"));

            pdfFile = new File(pdfPath, "invoice_" + newRowId + "_" + frmtted + ".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Load the image from assets
            AssetManager assetManager = context.getAssets();
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

//            View rootView = findViewById(android.R.id.content);
//            Snackbar.make(rootView, "Invoice generated "+newRowId, Snackbar.LENGTH_LONG)
//                    .setDuration(5000)
//                    .setAction("View", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            openGeneratedPDF(pdfFile);
//                            // Create an Intent to open the generated invoice
//                        }
//                    })
//                    .show();
//
//


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


        return pdfFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String getFormattedDateTime(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
        String formattedDate = dateTime.format(outputFormatter);
        return formattedDate;
    }


}
