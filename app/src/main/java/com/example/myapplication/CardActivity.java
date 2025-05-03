package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card);

        TextView textView = findViewById(R.id.detail_title);
        TextView textView1 = findViewById(R.id.detail_title1);

        View circleDot = findViewById(R.id.circle_dot);
        View circleDot1 = findViewById(R.id.circle_dot1);

        textView.setText("Kokam");
        textView1.setText("Orange");

    }

    private int getRandomColor() {
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        return color;
    }
}