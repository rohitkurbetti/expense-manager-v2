package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.ConfigAdapter;
import com.example.myapplication.dtos.ConfigItem;

import java.util.ArrayList;
import java.util.List;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        List<ConfigItem> itemList = new ArrayList<>();
        itemList.add(new ConfigItem("Set item prices", R.drawable.price_tag_svgrepo_com));
        itemList.add(new ConfigItem("Add / Remove items", R.drawable.panel_direction_svgrepo_com));
//        itemList.add(new ConfigItem("Banana", R.drawable.banana));
//        itemList.add(new ConfigItem("Cherry", R.drawable.cherry));
//        itemList.add(new ConfigItem("Date", R.drawable.date));
//        itemList.add(new ConfigItem("Grapes", R.drawable.grapes));
//        itemList.add(new ConfigItem("Mango", R.drawable.mango));

        ConfigAdapter adapter = new ConfigAdapter(getApplicationContext(), itemList);
        recyclerView.setAdapter(adapter);




    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes the activity and returns to the previous one
        return true;
    }
}