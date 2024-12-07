package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bar_chart);

        Intent intent = getIntent();
        Map<String, Integer> itemsSold = (Map<String, Integer>) intent.getSerializableExtra("map");

        // Find the BarChart view
        BarChart barChart = findViewById(R.id.barChart);

        // Prepare the data
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : itemsSold.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Create a data set
        BarDataSet dataSet = new BarDataSet(entries, "Items Sold");
        dataSet.setColors(new int[]{R.color.cyan,R.color.orange},R.color.yellow1);

        // Create BarData with the data set
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f); // set custom bar width

        // Set the data and customize the chart
        barChart.setData(barData);
        barChart.setFitBars(false); // make the x-axis fit exactly all bars
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getAxisRight().setEnabled(false); // disable right y-axis
        barChart.animateY(1000); // animate the chart
        barChart.invalidate(); // refresh the chart


















//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}