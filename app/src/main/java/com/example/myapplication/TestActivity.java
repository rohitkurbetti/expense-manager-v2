package com.example.myapplication;

import android.os.Bundle;
import android.widget.ListView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.Expense;
import com.example.myapplication.adapters.ExpenseAdapter;

import java.util.ArrayList;
import java.util.List;

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