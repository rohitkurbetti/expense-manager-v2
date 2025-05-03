package com.example.myapplication.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PDFGenerator {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void generateInvoicePDF(Context context, String itemListJson) {
        // Create a new PdfDocument
        PdfDocument document = new PdfDocument();

        // Create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();

        // Start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get the Canvas object for the page
        Canvas canvas = page.getCanvas();

        // Create a Paint object for drawing
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);

        // Draw text on the page
        canvas.drawText("Invoice", 40, 50, paint);

        paint.setTextSize(8);
        canvas.drawText(itemListJson, 40, 90, paint);

        // Finish the page
        document.finishPage(page);

        // Create a file to save the PDF
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File file = new File(path, "invoice.pdf");

        try {
            // Write the document content to the file
            document.writeTo(Files.newOutputStream(file.toPath()));

            // Close the document
            document.close();

            // Show a toast message indicating successful PDF generation
            Toast.makeText(context, "Invoice PDF generated successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show a toast message indicating an error occurred
            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
