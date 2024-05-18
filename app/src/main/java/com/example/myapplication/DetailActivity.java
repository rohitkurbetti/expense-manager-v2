package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView subtitleTextView;
    private TextView timestampTextView;
    private TextView jsonTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        titleTextView = findViewById(R.id.titleTextView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        jsonTextView = findViewById(R.id.jsonTextView);

        DataModel dataModel = (DataModel) getIntent().getSerializableExtra("dataModel");

        if (dataModel != null) {
            titleTextView.setText(dataModel.getTitle());
            subtitleTextView.setText(dataModel.getSubtitle());
            timestampTextView.setText(dataModel.getTimestamp());
            jsonTextView.setText(dataModel.getProfileImageUrl());
        }


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}