package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.BarChartDialog;
import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapters.InvoiceAdapter;
import com.example.myapplication.database.DatabaseHelper;
import com.example.myapplication.dtos.DtoJson;
import com.example.myapplication.dtos.Invoice;
import com.example.myapplication.dtos.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InvoicesFragment extends Fragment {

    private static final String INR_SYMBOL = "₹";
    int gCounter = 1;
    private EditText edtFilterDate;
    private ProgressDialog progressDialog;
    private TextView totalAmountTextView;
    private TextView totalRecordsTextView;
    private ListView listView;
    private InvoiceAdapter adapter;
    private List<Invoice> invoiceList, filteredList;
    private DatabaseHelper db;
    private ImageView noSqliteDataImageView;
    private TextView noSqliteDataTextView;
    private ImageView deleteBtn;
    private LinearLayout selectionOverlay;
    public static TextView itemSelectedTxt;
    private ImageView invoiceFilterButton;
    private LinearLayout invoicesFilterHeader;
    private ImageView barChartMonthly, barReportBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoices, container, false);

        db = new DatabaseHelper(view.getContext());
        progressDialog = new ProgressDialog(getContext());
        listView = view.findViewById(R.id.listView);
        edtFilterDate = view.findViewById(R.id.edtFilterDate);
        Button resetFilterBtn = view.findViewById(R.id.resetFilterBtn);
        totalRecordsTextView = view.findViewById(R.id.totalRecordsTextView);
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        noSqliteDataImageView = view.findViewById(R.id.noSqliteDataImageView);
        noSqliteDataTextView = view.findViewById(R.id.noSqliteDataTextView);
        deleteBtn = view.findViewById(R.id.deleteBtn);
        selectionOverlay = view.findViewById(R.id.selectionOverlay);
        itemSelectedTxt = view.findViewById(R.id.itemSelectedTxt);
        invoiceFilterButton = view.findViewById(R.id.invoiceFilterButton);
        invoicesFilterHeader = view.findViewById(R.id.invoicesFilterHeader);
        barChartMonthly = view.findViewById(R.id.barChartMonthly);
        barReportBtn = view.findViewById(R.id.barReportBtn);

        invoiceFilterButton.setOnClickListener(v -> toggleInvoicesFilter());
        invoicesFilterHeader.setOnClickListener(v -> toggleInvoicesFilter());

        barReportBtn.setOnClickListener(v -> showBarChartPopup());

        invoiceList = new ArrayList<>();
        filteredList = new ArrayList<>();

        barChartMonthly.setOnClickListener(v -> openBarChartDialog());


        edtFilterDate.setOnClickListener(v -> showDateTimeDialog(false));
        edtFilterDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDateTimeDialog(true);
                return true;
            }
        });
        resetFilterBtn.setOnClickListener(v -> resetFilters());
        filteredList.addAll(invoiceList);


        adapter = new InvoiceAdapter(view.getContext(), filteredList, selectionOverlay);
        listView.setAdapter(adapter);

        try {
            getAllInvoicesFromInDb(progressDialog, adapter, filteredList);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        deleteBtn.setOnClickListener(v -> {
            try {
                deleteSelectedInvoicesById();
                selectionOverlay.setVisibility(View.GONE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    private void showBarChartPopup() {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.radio_popup, null);

        final RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Reports");
        builder.setIcon(R.drawable.report_svgrepo_com);
        builder.setView(view);
        builder.setPositiveButton("Generate", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = view.findViewById(selectedId);
                String selectedText = selectedRadio.getText().toString();


                switch (selectedText) {
                    case "Monthly":
                        printBarChart(12);
                        break;
                    case "Quarterly":
                        printBarChart(4);
                        break;
                    case "Yearly":
                        printBarChart(1);
                        break;
                    default:
                        break;
                }


            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void printBarChart(int groupSize) {

        Cursor cursor = db.getPeriodRecords("2025-01-01", "2025-12-31");

        List<DtoJson> grandItemsList = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String itemListJson = cursor.getString(1);

                ObjectMapper objectMapper = new ObjectMapper();

                DtoJson itemObject = null;
                try {
                    itemObject = objectMapper.readValue(itemListJson, DtoJson.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                grandItemsList.add(itemObject);


            }
        }
        db.close();
        for (int i = 1; i <= groupSize; i++) {
            Map<String, Integer[]> itemSaleMap = new HashMap<>();

            //filter grandList
            String unitStr = "Quarter " + String.format("%02d", i);

            List<DtoJson> filteredList = new ArrayList<>();

            if (groupSize == 4) {
                unitStr = "Quarter " + String.format("%02d", i);
                for (int ctr = 1; ctr <= 3; ctr++) {

//                String mth = "2025-"+ String.format("%02d", gCounter++);
                    List<DtoJson> filteredListTemp = grandItemsList.stream()
                            .filter(dtoJson -> dtoJson.getDate().startsWith("2025-" + String.format("%02d", gCounter)))
                            .collect(Collectors.toList());

                    if (filteredListTemp.isEmpty()) {
                        gCounter++;
                        continue;
                    }

                    filteredList.addAll(filteredListTemp);
                    Log.i("inner loop", String.valueOf("2025-" + gCounter + " >>> " + filteredList.size()));
                    gCounter++;

                }
            } else if (groupSize == 12) {
                unitStr = "Month " + String.format("%02d", i);
                List<DtoJson> filteredListTemp = grandItemsList.stream()
                        .filter(dtoJson -> dtoJson.getDate().startsWith("2025-" + String.format("%02d", gCounter)))
                        .collect(Collectors.toList());

                if (filteredListTemp.isEmpty()) {
                    gCounter++;
                    continue;
                }

                filteredList.addAll(filteredListTemp);
                Log.i("inner loop", String.valueOf("2025-0" + gCounter + " >>> " + filteredList.size()));
                gCounter++;

            } else {
                unitStr = "Year " + String.format("%02d", i);
                for (int ctr = 1; ctr <= 12; ctr++) {
                    List<DtoJson> filteredListTemp = grandItemsList.stream()
                            .filter(dtoJson -> dtoJson.getDate().startsWith("2025-" + String.format("%02d", gCounter)))
                            .collect(Collectors.toList());

                    if (filteredListTemp.isEmpty()) {
                        gCounter++;
                        continue;
                    }

                    filteredList.addAll(filteredListTemp);
                    Log.i("inner loop", String.valueOf("2025-0" + gCounter + " >>> " + filteredList.size()));
                    gCounter++;
                }
            }


            Log.i("outer loop", String.valueOf(filteredList.size()));
            filteredList.forEach(itemObject -> {
                List<CustomItem> items = itemObject.getItemList();
                for (CustomItem item : items) {
                    int qty = 0;
                    int amount = 0;
                    if (itemSaleMap.containsKey(item.getName())) {
                        int tempQty = itemSaleMap.get(item.getName())[1];
                        qty = (int) item.getSliderValue() + tempQty;
                        amount = item.getAmount();
                        itemSaleMap.put(item.getName(), new Integer[]{amount, qty});
                    } else {
                        amount = 0;
                        qty = (int) item.getSliderValue();
                        amount = amount + item.getAmount();
                        itemSaleMap.put(item.getName(), new Integer[]{amount, (int) item.getSliderValue()});

                    }
                }
            });


            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<Item> items = new ArrayList<>();

            int ctr = 0;
            for (Map.Entry<String, Integer[]> map : itemSaleMap.entrySet()) {
                entries.add(new BarEntry(ctr++, map.getValue()[1]));
                if (!items.contains(map.getKey())) {
                    items.add(new Item(map.getKey(), map.getValue()[1], map.getValue()[0]));
                }
                labels.add(map.getKey());
            }
            if (!entries.isEmpty()) {
                generateBarChartPdf(getContext(), entries, labels, items, unitStr);
            }
        }
        gCounter = 1;
    }

    private void generateBarChartPdf(Context context, List<BarEntry> entries, List<String> labels, List<Item> itemList, String unitStr) {

        int pageWidth = 595; // A4 width in points
        int pageHeight = 842; // A4 height in points
        int margin = 40;
        int availableWidth = pageWidth - 2 * margin;
        int chartHeight = 300; // height for chart

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();

        canvas.drawText(unitStr, 150, 70, paint);

        // === Create BarChart ===
        BarChart barChart = new BarChart(context);
        barChart.setLayoutParams(new ViewGroup.LayoutParams(availableWidth, chartHeight));

        BarDataSet dataSet = new BarDataSet(entries, "Items");
        dataSet.setColor(Color.parseColor("#3F51B5"));
        dataSet.setValueTextSize(7f);

        BarData data = new BarData(dataSet);
        data.setValueTextSize(6f);
        data.setBarWidth(0.3f); // makes bars take up the correct space
        barChart.setData(data);
        barChart.setFitBars(true); // makes the x-axis fit exactly to the bars

        barChart.getXAxis().setTextSize(5f);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.getXAxis().setLabelRotationAngle(330f); // Adjust if labels are too long
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
//        barChart.setExtraBottomOffset(10f); // Add some space at the bottom


        barChart.invalidate();

        // Layout chart view
        barChart.measure(
                View.MeasureSpec.makeMeasureSpec(availableWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(chartHeight, View.MeasureSpec.EXACTLY));
        barChart.layout(0, 0, barChart.getMeasuredWidth(), barChart.getMeasuredHeight());

        // Capture chart bitmap
        Bitmap chartBitmap = Bitmap.createBitmap(barChart.getMeasuredWidth(), barChart.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas chartCanvas = new Canvas(chartBitmap);
        barChart.draw(chartCanvas);

        // Scale and draw chart bitmap
        float scale = (float) availableWidth / chartBitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(margin, margin);

        canvas.drawBitmap(chartBitmap, matrix, null);

        // Step 2: Draw Table Header
        int startY = margin + chartHeight + 50;
        int colX1 = 50;
        int colX2 = 200;
        int colX3 = 300;
        int colX4 = 400;
        int colX5 = 500;


        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Item Name", colX1, startY, paint);
        canvas.drawText("Rate", colX2, startY, paint);
        canvas.drawText("Qty", colX3, startY, paint);
        canvas.drawText("Amount", colX4, startY, paint);

        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        int lineY = startY + 20;


        // Step 3: Draw Table Rows
        for (Item item : itemList) {
            canvas.drawText(item.getItemName(), colX1, lineY, paint);
            canvas.drawText(String.valueOf(item.getItemAmount() / item.getItemQty()), colX2, lineY, paint);
            canvas.drawText(String.valueOf(item.getItemQty()), colX3, lineY, paint);
            canvas.drawText(String.valueOf(item.getItemAmount()), colX4, lineY, paint);
            lineY += 20;
        }

        pdfDocument.finishPage(page);

        // Step 4: Save PDF
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        String letters = new SimpleDateFormat("yy-MM-dd_HHmmss").format(new Date());

        String bth = downloadsDir + "/Asd/" + letters;

        File fileCheck = new File(bth);

        if (!fileCheck.exists()) {
            fileCheck.mkdirs();
        }


        File file = new File(bth, "bar_chart_table_" + unitStr + ".pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            Toast.makeText(context, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();


    }

    private void openBarChartDialog() {

        String date = filteredList.get(0).getCreatedDate();
        Map<String, Integer[]> map = db.getInvoicesByDate(date);


        BarChartDialog dialog = new BarChartDialog(requireContext(), map);
        dialog.show();
    }

    private void toggleInvoicesFilter() {
        int visibilityGone = View.GONE;
        if (visibilityGone == invoicesFilterHeader.getVisibility()) {
            invoicesFilterHeader.setVisibility(View.VISIBLE);
            invoiceFilterButton.setVisibility(visibilityGone);
            barChartMonthly.setVisibility(View.GONE);
        } else {
            invoicesFilterHeader.setVisibility(visibilityGone);
            invoiceFilterButton.setVisibility(View.VISIBLE);
            barChartMonthly.setVisibility(View.GONE);

        }

    }

    private void deleteSelectedInvoicesById() throws IOException {

        IntStream invIdStream = invoiceList.stream().filter(Invoice::getChecked).mapToInt(Invoice::getInvoiceId);
        Set<Integer> invIds = invIdStream.boxed().collect(Collectors.toSet());

        db.deleteInvoicesbyIds(invIds);

        db.deleteFirestoreInvoicesbyIds(invoiceList, progressDialog);
        getAllInvoicesFromInDb(progressDialog, adapter, filteredList);
        resetAllInvoiceCheckBoxes();
        if (!invoiceList.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(invoiceList);
        }
        adapter.notifyDataSetChanged();


    }

    private void resetAllInvoiceCheckBoxes() {
        invoiceList.stream().map(invoice -> {
            invoice.setChecked(false);
            return invoice;
        });
//        List<Invoice> checkedList = invoiceList.stream().collect(Collectors.toList());
//        Log.d(">>>", checkedList.toString());
        selectionOverlay.setVisibility(View.GONE);

    }

    private void resetFilters() {
        filterInvoices("");
        barChartMonthly.setVisibility(View.GONE);
        edtFilterDate.setText(null);
    }

    private void showDateTimeDialog(boolean monthWise) {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();

        if (monthWise) {
            new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.set(year, month, 1);
                    CharSequence seq = android.text.format.DateFormat.format("yyyy-MM", date);
                    CharSequence yearMonth = android.text.format.DateFormat.format("MMMM-yyyy", date);
                    edtFilterDate.setText(String.valueOf(yearMonth).toUpperCase());
                    filterInvoices(String.valueOf(seq));
                }
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
        } else {
            new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.set(year, month, dayOfMonth);
                    edtFilterDate.setText(android.text.format.DateFormat.format("yyyy-MM-dd", date));
                    filterInvoices(String.valueOf(edtFilterDate.getText()));
                }
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
        }
    }

    private void filterInvoices(String date) {
        barChartMonthly.setVisibility(View.VISIBLE);
        filteredList.clear();
        if (date.isEmpty()) {
            filteredList.addAll(invoiceList);
        } else {
            for (Invoice invoice : invoiceList) {
                if (invoice.getCreatedDate().contains(date)) {
                    filteredList.add(invoice);
                }
            }
        }
        adapter.updateList(filteredList);

        totalRecordsTextView.setText("Total Records: " + filteredList.size());
        OptionalInt totalSumOptional = filteredList.stream().mapToInt(i -> (int) i.getTotal()).reduce((a, b) -> a + b);
        int totalSum = 0;
        if (totalSumOptional.isPresent()) {
            totalSum = totalSumOptional.getAsInt();
        }

        totalAmountTextView.setText("Total: " + INR_SYMBOL + totalSum);


    }

    private void getAllInvoicesFromInDb(ProgressDialog progressDialog, InvoiceAdapter adapter, List<Invoice> filteredList) throws IOException {
        long startTime = System.currentTimeMillis();

        Cursor cursor = db.getAllInvoices();

        new Thread(new Runnable() {
            private int totalSum = 0;
            int totalList = cursor.getCount();

            @Override
            public void run() {

                if (cursor.getCount() > 0) {

                    int progressCount = 0;
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Invoice Data");
                        progressDialog.show();
                        listView.setVisibility(View.VISIBLE);
                        noSqliteDataImageView.setVisibility(View.GONE);
                        noSqliteDataTextView.setVisibility(View.GONE);
                    });


                    invoiceList.clear();
                    int qty = 0;
                    int amount = 0;
                    while (cursor.moveToNext()) {
                        int invId = cursor.getInt(0);
                        String itemListJson = cursor.getString(1);
                        long total = cursor.getLong(2);
                        String createdDateTime = cursor.getString(3);
                        String createdDate = cursor.getString(4);

                        ObjectMapper objectMapper = new ObjectMapper();

                        DtoJson itemObject = null;
                        try {
                            itemObject = objectMapper.readValue(itemListJson, DtoJson.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }

                        List<CustomItem> items = itemObject.getItemList();
                        List<CustomItem> otherItems = itemObject.getOtherItemsList();

                        if (!otherItems.isEmpty()) {
                            items.addAll(otherItems);
                        }

                        Map<String, Integer[]> itemSaleMap = new HashMap<>();

                        for (CustomItem i : items) {
                            if (itemSaleMap.containsKey(i.getName())) {
                                int tempQty = itemSaleMap.get(i.getName())[1];
                                qty = (int) i.getSliderValue() + tempQty;
                                amount = i.getAmount();
                                itemSaleMap.put(i.getName(), new Integer[]{amount, qty});
                            } else {
                                amount = 0;
                                qty = (int) i.getSliderValue();
                                amount = amount + i.getAmount();
                                itemSaleMap.put(i.getName(), new Integer[]{amount, (int) i.getSliderValue()});


                            }
                        }
                        invoiceList.add(new Invoice(invId, itemListJson, total, createdDateTime, createdDate, itemSaleMap));

                        int progress = (int) (((progressCount + 1) / (float) totalList) * 100);
                        // Update progress bar and text on UI thread
                        int finalProgressCount = progressCount;
                        requireActivity().runOnUiThread(() -> {
//                            progressDialog.setProgress(progress);
// ** do not remove           progressDialog.setMessage("Processed "+finalProgressCount +"/"+totalList +" records  "+ progress + "%");
                            progressDialog.setMessage("Fetching records " + progress + "%");
                        });
                        progressCount++;

                    }

                    requireActivity().runOnUiThread(() -> {
                        totalRecordsTextView.setText("Total Records: " + cursor.getCount());

                    });
                    OptionalInt totalSumOptional = invoiceList.stream().mapToInt(i -> (int) i.getTotal()).reduce((a, b) -> a + b);
                    if (totalSumOptional.isPresent()) {
                        totalSum = totalSumOptional.getAsInt();
                    }
                    requireActivity().runOnUiThread(() -> {
                        totalAmountTextView.setText("Total: " + INR_SYMBOL + totalSum);

                        filteredList.clear();
                        filteredList.addAll(invoiceList);
                        adapter.notifyDataSetChanged();


                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        listView.setVisibility(View.GONE);
                        noSqliteDataImageView.setVisibility(View.VISIBLE);
                        noSqliteDataTextView.setVisibility(View.VISIBLE);
                        totalRecordsTextView.setText("Total Records: " + cursor.getCount());
                        totalAmountTextView.setText("Total: " + INR_SYMBOL + "0");
                    });

                }
//                totalSum=0;
                long endTime = System.currentTimeMillis();
                long elapsedMillis = endTime - startTime;

                requireActivity().runOnUiThread(() -> {
                    String timeFormatted = formatMillisToMinSec(elapsedMillis);

                    Toast.makeText(getContext(), "Execution time: " + timeFormatted, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
            }
        }).start();
    }

    public String formatMillisToMinSec(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%dm%ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}

