package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        ListView listView = findViewById(R.id.listView);

//        ExpenseAdapter adapter = new ExpenseAdapter(this, expenses);
//        listView.setAdapter(adapter);

    }
}