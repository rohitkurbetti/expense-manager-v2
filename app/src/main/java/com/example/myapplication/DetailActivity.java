package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
//    private TextView titleTextView;
//    private TextView subtitleTextView;
//    private TextView timestampTextView;
//    private TextView jsonTextView;

    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        DataModel dataModel = (DataModel) getIntent().getSerializableExtra("dataModel");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        titleTextView = findViewById(R.id.titleTextView);
//        subtitleTextView = findViewById(R.id.subtitleTextView);
//        timestampTextView = findViewById(R.id.timestampTextView);
//        jsonTextView = findViewById(R.id.jsonTextView);




//            titleTextView.setText(dataModel.getTitle());
//            subtitleTextView.setText(dataModel.getSubtitle());
//            timestampTextView.setText(dataModel.getTimestamp());
//            jsonTextView.setText(dataModel.getProfileImageUrl());




            JsonObject jsonObject = (new JsonParser()).parse(dataModel.getProfileImageUrl())
                    .getAsJsonObject();

            JsonArray listArr = jsonObject.getAsJsonArray("itemList");

            Gson gson = new Gson();
            Type listType = new TypeToken<List<CustomItem>>() {}.getType();

            List<CustomItem> itemList = gson.fromJson(listArr, listType);

            dataModel.setItemList(itemList);

            List list = new ArrayList<>();
            list.add(dataModel);

            UserAdapter adapter = new UserAdapter(list);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();




//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}