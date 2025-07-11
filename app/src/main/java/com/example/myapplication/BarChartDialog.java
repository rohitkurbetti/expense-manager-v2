package com.example.myapplication;

// BarChartDialog.java
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BarChartDialog extends Dialog {

    private final Map<String, Integer[]> map;
    private BarChart barChart;
    private Button btnCloseDialog;

    public BarChartDialog(@NonNull Context context, Map<String, Integer[]> map) {
        super(context);
        this.map = map;
    }

    // This is the key method to make the dialog wide
    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            // Set the width to match_parent and height to wrap_content
            // You can also use fixed pixel values or percentages if you need more control
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Optional: Remove default dialog padding if desired
            // window.setBackgroundDrawableResource(android.R.color.transparent);
            // This line would make the dialog's background transparent, allowing you to control
            // the rounded corners and transparency purely from your dialog_bar_chart.xml
            // If dialog_bar_chart.xml already has a background with padding, you might not need this.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bar_chart);

        barChart = findViewById(R.id.barChart);
        btnCloseDialog = findViewById(R.id.btnCloseDialog);

        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Close the dialog
            }
        });

        setupBarChart();
    }

    private void setupBarChart() {
        // 1. Create BarEntry objects (data points)
        List<BarEntry> entries = new ArrayList<>();

        String[] quarters = new String[map.size()];


        AtomicInteger atomicInteger = new AtomicInteger(0);
        map.forEach((k,v) -> {

            entries.add(new BarEntry(atomicInteger.getAndAdd(1), v[1])); // Quarter 1
            quarters[atomicInteger.get()-1]=k;
        });


        // 2. Create a BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Nice predefined colors
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        // 3. Create BarData from DataSet
        BarData barData = new BarData(dataSet);
//        barData.setBarWidth(0.9f); // Set custom bar width

        // 4. Set BarData to BarChart
        barChart.setData(barData);

        // 5. Customize the chart appearance
        barChart.setFitBars(false); // Make the bars fit the viewport
        barChart.animateY(1000); // Animation for Y-axis

        // Enable dragging (horizontal scrolling)
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false); // optional
        barChart.setPinchZoom(false);

        // Set how many bars are visible at once
        barChart.setVisibleXRangeMaximum(5); // Show 5 bars at a time
        barChart.moveViewToX(0);


        // Customize X-axis (labels for quarters)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP); // Labels at the bottom
        xAxis.setDrawGridLines(false); // No vertical grid lines
        xAxis.setGranularity(1f); // Minimum interval between values
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);


        xAxis.setValueFormatter(new IndexAxisValueFormatter(quarters)); // Custom labels

        // Customize Y-axis (left and right)
        barChart.getAxisRight().setEnabled(false); // Disable right Y-axis
        barChart.getAxisLeft().setAxisMinimum(0f); // Start Y-axis from 0
        barChart.getAxisLeft().setTextSize(12f);
        barChart.getAxisLeft().setTextColor(Color.BLACK);

        // Customize Description (label at bottom right)
        Description description = new Description();
        description.setText("");
        description.setTextSize(10f);
        barChart.setDescription(description);

        barChart.invalidate(); // Refresh the chart
    }
}