package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.ProductAdapter;
import com.example.myapplication.dtos.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NewUIActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    public static List<Product> productList2 = new ArrayList<>();
    public static List<Product> productList3= new ArrayList<>();
    public static List<Product> productList4= new ArrayList<>();
    private Handler handler;
    private Runnable updateJsonRunnable;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static final String PREFS_NAME = "StoreData";
    public static final String SELECTED_PRODUCTS_KEY = "selectedProducts";
    public static final String SELECTED_PRODUCTS_KEY2 = "selectedProducts2";
    public static final String SELECTED_PRODUCTS_KEY3 = "selectedProducts3";
    public static final String SELECTED_PRODUCTS_KEY4 = "selectedProducts4";
    private static final long PERIODIC_SAVE_DELAY_MS = 995000; // Save every 5 seconds


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_uiactivity);


        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize product list with your items
        productList = new ArrayList<>();
        productList.add(new Product("Lemon", 0));
        productList.add(new Product("Orange", 0));
        productList.add(new Product("Kokum", 0));
        productList.add(new Product("Sarbat", 0));

        productList2 = new ArrayList<>();
        productList2.add(new Product("Lemon", 0));
        productList2.add(new Product("Orange", 0));
        productList2.add(new Product("Kokum", 0));
        productList2.add(new Product("Sarbat", 0));

        productList3 = new ArrayList<>();
        productList3.add(new Product("Lemon", 0));
        productList3.add(new Product("Orange", 0));
        productList3.add(new Product("Kokum", 0));
        productList3.add(new Product("Sarbat", 0));

        productList4 = new ArrayList<>();
        productList4.add(new Product("Lemon", 0));
        productList4.add(new Product("Orange", 0));
        productList4.add(new Product("Kokum", 0));
        productList4.add(new Product("Sarbat", 0));


        // Initialize SharedPreferences and Gson
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        // Load previously saved quantities into the productList
        loadSelectedProducts();

        // Initialize adapter and set to RecyclerView
        productAdapter = new ProductAdapter(this, productList, unused -> saveSelectedProducts());
        recyclerViewProducts.setAdapter(productAdapter);

        // Setup periodic JSON update
        handler = new Handler(Looper.getMainLooper());
        updateJsonRunnable = new Runnable() {
            @Override
            public void run() {
                saveSelectedProducts(); // Call the saving method
                handler.postDelayed(this, PERIODIC_SAVE_DELAY_MS); // Reschedule
            }
        };

        // Start periodic updates
        handler.postDelayed(updateJsonRunnable, PERIODIC_SAVE_DELAY_MS);

        // Call to print all shared preferences content for debugging
        printAllSharedPreferences();



    }

    // Method to load selected product quantities from SharedPreferences
    private void loadSelectedProducts() {

        //save list2
        String json2 = gson.toJson(productList2);
        sharedPreferences.edit().putString(SELECTED_PRODUCTS_KEY2, json2).apply();

        //save list3
        String json3 = gson.toJson(productList3);
        sharedPreferences.edit().putString(SELECTED_PRODUCTS_KEY3, json3).apply();

        //save list4
        String json4 = gson.toJson(productList4);
        sharedPreferences.edit().putString(SELECTED_PRODUCTS_KEY4, json4).apply();


        String json = sharedPreferences.getString(SELECTED_PRODUCTS_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<Product>>() {}.getType();
            List<Product> savedProducts = gson.fromJson(json, type);

            // Update quantities in the main productList from saved data
            for (Product savedProduct : savedProducts) {
                for (Product currentProduct : productList) {
                    if (currentProduct.getName().equals(savedProduct.getName())) {
                        currentProduct.setQuantity(savedProduct.getQuantity());
                        break;
                    }
                }
            }
        }



    }

    // Method to save current selected product quantities to SharedPreferences
    private void saveSelectedProducts() {
        // Filter out products with quantity 0 before saving to keep the JSON clean
        List<Product> productsToSave = new ArrayList<>();
        for (Product product : productList) {
            if (product.getQuantity() > 0) {
                productsToSave.add(product);
            }
        }
        String json = gson.toJson(productsToSave);
        sharedPreferences.edit().putString(SELECTED_PRODUCTS_KEY, json).apply();
        Log.d("MainActivity", "Saved JSON: " + json); // Uncomment for debugging




    }

    // New method to print all SharedPreferences content
    private void printAllSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        java.util.Map<String, ?> allEntries = sharedPreferences.getAll();
        for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("SharedPreferences", "Key: " + entry.getKey() + " -> Value: " + entry.getValue());
        }
        if (allEntries.isEmpty()) {
            Log.d("SharedPreferences", "SharedPreferences is empty.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop periodic updates when the activity is destroyed to prevent memory leaks
        handler.removeCallbacks(updateJsonRunnable);
    }
}