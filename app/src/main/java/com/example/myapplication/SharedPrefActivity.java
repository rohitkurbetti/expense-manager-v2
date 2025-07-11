package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapterholders.SharedPrefItem;
import com.example.myapplication.adapters.SharedPrefAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SharedPrefActivity extends AppCompatActivity {
    private static final String SHARED_PREFS_FILE = "my_shared_prefs";
    private SharedPreferences sharedPreferences;
    private static final String FIRST_LAUNCH_KEY = "isFirstLaunch";

    private RecyclerView recyclerView;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_pref);
        recyclerView = findViewById(R.id.recyclerView);
        saveBtn = findViewById(R.id.saveBtn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        List<SharedPrefItem> itemList = new ArrayList<>();
        List<CustomItem> itemList1 = new ArrayList<>();
        itemList1 = (List<CustomItem>) getIntent().getSerializableExtra("sharedPrefList");

        itemList1.forEach(item -> {
            if(item.getName().equalsIgnoreCase("L. Lemon") || item.getName().equalsIgnoreCase("L. Orange") ||
                    item.getName().equalsIgnoreCase("Stwbry Soda") || item.getName().equalsIgnoreCase("Pineapple Soda") ||
                    item.getName().equalsIgnoreCase("S Sarbat")) {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),25)));
            } else if (item.getName().equalsIgnoreCase("Lassi_F")) {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),40)));
            } else if (item.getName().equalsIgnoreCase("Water_H")) {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),10)));
            } else if (item.getName().equalsIgnoreCase("Mng_Lssi_H")) {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),30)));
            } else if (item.getName().equalsIgnoreCase("Mng_Lssi_F")) {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),60)));
            } else {
                itemList.add(new SharedPrefItem(item.getName(), sharedPreferences.getInt(item.getName().toUpperCase(),20)));
            }
        });

//        SharedPrefItem sharedPrefItem = new SharedPrefItem("Kokam", sharedPreferences.getInt("Kokam".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem1 = new SharedPrefItem("Orange", sharedPreferences.getInt("Orange".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem2 = new SharedPrefItem("L. Lemon", sharedPreferences.getInt("L. Lemon".toUpperCase(),25));
//        SharedPrefItem sharedPrefItem3 = new SharedPrefItem("L. Orange", sharedPreferences.getInt("L. Orange".toUpperCase(),25));
//        SharedPrefItem sharedPrefItem4 = new SharedPrefItem("Sarbat", sharedPreferences.getInt("Sarbat".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem5 = new SharedPrefItem("S Sarbat", sharedPreferences.getInt("S Sarbat".toUpperCase(),25));
//        SharedPrefItem sharedPrefItem6 = new SharedPrefItem("Pachak", sharedPreferences.getInt("Pachak".toUpperCase(),25));
//        SharedPrefItem sharedPrefItem7 = new SharedPrefItem("L. Soda", sharedPreferences.getInt("L. Soda".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem8 = new SharedPrefItem("Wala", sharedPreferences.getInt("Wala".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem9 = new SharedPrefItem("Lassi_H", sharedPreferences.getInt("Lassi_H".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem10 = new SharedPrefItem("Lassi_F", sharedPreferences.getInt("Lassi_F".toUpperCase(),40));
//        SharedPrefItem sharedPrefItem11 = new SharedPrefItem("J. Soda", sharedPreferences.getInt("J. Soda".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem12 = new SharedPrefItem("Taak", sharedPreferences.getInt("Taak".toUpperCase(),10));
//        SharedPrefItem sharedPrefItem13 = new SharedPrefItem("Kulfi", sharedPreferences.getInt("Kulfi".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem14 = new SharedPrefItem("Stwbry Soda", sharedPreferences.getInt("Stwbry Soda".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem15 = new SharedPrefItem("Water_H", sharedPreferences.getInt("Water_H".toUpperCase(),10));
//        SharedPrefItem sharedPrefItem16 = new SharedPrefItem("Water_F", sharedPreferences.getInt("Water_F".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem17 = new SharedPrefItem("Mng_Lssi_H", sharedPreferences.getInt("Mng_Lssi_H".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem18 = new SharedPrefItem("Mng_Lssi_F", sharedPreferences.getInt("Mng_Lssi_F".toUpperCase(),40));
//        SharedPrefItem sharedPrefItem19 = new SharedPrefItem("Btrsch", sharedPreferences.getInt("Btrsch".toUpperCase(),20));
//        SharedPrefItem sharedPrefItem20 = new SharedPrefItem("Sample", sharedPreferences.getInt("Sample".toUpperCase(),20));
//
//        itemList.add(sharedPrefItem);
//        itemList.add(sharedPrefItem1);
//        itemList.add(sharedPrefItem2);
//        itemList.add(sharedPrefItem3);
//        itemList.add(sharedPrefItem4);
//        itemList.add(sharedPrefItem5);
//        itemList.add(sharedPrefItem6);
//        itemList.add(sharedPrefItem7);
//        itemList.add(sharedPrefItem8);
//        itemList.add(sharedPrefItem9);
//        itemList.add(sharedPrefItem10);
//        itemList.add(sharedPrefItem11);
//        itemList.add(sharedPrefItem12);
//        itemList.add(sharedPrefItem13);
//        itemList.add(sharedPrefItem14);
//        itemList.add(sharedPrefItem15);
//        itemList.add(sharedPrefItem16);
//        itemList.add(sharedPrefItem17);
//        itemList.add(sharedPrefItem18);
//        itemList.add(sharedPrefItem19);
//        itemList.add(sharedPrefItem20);

        SharedPrefAdapter adapter = new SharedPrefAdapter(itemList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedPrefs(itemList, adapter);
                markFirstLaunchDone();

            }
        });


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default": setTheme(R.style.Base_Theme_MyApplication); break;
            case "Red": setTheme(R.style.AppTheme_Red); break;
            case "Blue": setTheme(R.style.AppTheme_Blue); break;
            case "Green": setTheme(R.style.AppTheme_Green); break;
            case "Purple": setTheme(R.style.AppTheme_Purple); break;
            case "Orange": setTheme(R.style.AppTheme_Orange); break;
            case "Teal": setTheme(R.style.AppTheme_Teal); break;
            case "Pink": setTheme(R.style.AppTheme_Pink); break;
            case "Cyan": setTheme(R.style.AppTheme_Cyan); break;
            case "Lime": setTheme(R.style.AppTheme_Lime); break;
            case "Brown": setTheme(R.style.AppTheme_Brown); break;
            case "Mint": setTheme(R.style.AppTheme_Mint); break;
            case "Coral": setTheme(R.style.AppTheme_Coral); break;
            case "Steel": setTheme(R.style.AppTheme_Steel); break;
            case "Lavender": setTheme(R.style.AppTheme_Lavender); break;
            case "Mustard": setTheme(R.style.AppTheme_Mustard); break;
            default: setTheme(R.style.Base_Theme_MyApplication); break;
        }
    }

    private void markFirstLaunchDone() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_LAUNCH_KEY, false);
        editor.apply();
    }
    private void saveSharedPrefs(List<SharedPrefItem> itemList, SharedPrefAdapter adapter) {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        itemList.forEach(item -> {
            // Use the editor to put values into SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(item.getItemName().toUpperCase(Locale.getDefault()), item.getItemValue());
            // Commit the changes
            editor.apply();
        });
        Toast.makeText(this, "Item prices updated", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
        this.finish();


    }
}