package com.example.myapplication.adapters;


import static com.example.myapplication.NewUIActivity.SELECTED_PRODUCTS_KEY2;
import static com.example.myapplication.NewUIActivity.SELECTED_PRODUCTS_KEY3;
import static com.example.myapplication.NewUIActivity.SELECTED_PRODUCTS_KEY4;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.NewUIActivity;
import com.example.myapplication.R;
import com.example.myapplication.dtos.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer; // For functional interface


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String PREFS_NAME = "StoreData";


    private List<Product> productList;
    private Context context;
    private Consumer<Void> onQuantityChangedCallback; // Callback to notify MainActivity to save data

    // This map will store the intermediate quantity input string for each item
    // as the user types on the grid. Key: Product Name, Value: Current input string.
    private java.util.Map<String, StringBuilder> currentInputQuantities = new java.util.HashMap<>();
    private SharedPreferences sharedPreferences;

    private String SELECTED_PRODUCTS_KEY = "selectedProducts";
    private static Gson gson = new Gson();

    public ProductAdapter(Context context, List<Product> productList, Consumer<Void> onQuantityChangedCallback) {
        this.context = context;
        this.productList = productList;
        this.onQuantityChangedCallback = onQuantityChangedCallback;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Product product = productList.get(position);
        holder.productName.setText(product.getName());

        // Initialize the input StringBuilder for this product if not already present
        if (!currentInputQuantities.containsKey(product.getName())) {
            currentInputQuantities.put(product.getName(), new StringBuilder());
        }
        final StringBuilder currentInput = currentInputQuantities.get(product.getName());

        // Update the main action button's icon based on product quantity
        updateMainActionButton(holder, holder.buttonMainAction, product.getQuantity(), 0, 0, 0);

        // --- Listeners for Quantity Grid Buttons ---
        View.OnClickListener gridButtonListener = v -> {
            Button clickedButton = (Button) v;
            String buttonText = clickedButton.getText().toString();

            if (buttonText.equals("C")) { // Assuming 'C' for clear, though not in image, it's good practice
                currentInput.setLength(0);
                holder.textCurrentQuantityInput.setVisibility(View.GONE);
            } else if (buttonText.equals(".")) {
                if (currentInput.length() == 0) { // If '.' is first, prepend "0"
                    currentInput.append("0.");
                } else if (!currentInput.toString().contains(".")) {
                    currentInput.append(".");
                }
            } else if (buttonText.equals("+")) { // Treat '+' in grid as "apply current input"
                applyGridQuantity(holder, product, currentInput);
            } else { // It's a number
                // Prevent leading zero unless it's "0."
                if (currentInput.length() == 1 && currentInput.charAt(0) == '0' && !buttonText.equals(".")) {
                    currentInput.setLength(0); // Clear the single zero
                }
                currentInput.append(buttonText);
            }

            if (currentInput.length() > 0) {
                holder.textCurrentQuantityInput.setText(currentInput.toString());
                holder.textCurrentQuantityInput.setVisibility(View.VISIBLE);
            } else {
                holder.textCurrentQuantityInput.setVisibility(View.GONE);
            }
        };

        holder.buttonNum1.setOnClickListener(gridButtonListener);
        holder.buttonNum2.setOnClickListener(gridButtonListener);
        holder.buttonNum3.setOnClickListener(gridButtonListener);
        holder.buttonNum4.setOnClickListener(gridButtonListener);

        // --- Main Action Button (+/-) Listener ---
        holder.buttonMainAction.setOnClickListener(v -> {

            //get list2
            String json2 = sharedPreferences.getString(NewUIActivity.SELECTED_PRODUCTS_KEY2, "");
            List<Product> parsedList2 = parseJsonList(json2);

            //get list3
            String json3 = sharedPreferences.getString(NewUIActivity.SELECTED_PRODUCTS_KEY3, "");
            List<Product> parsedList3 = parseJsonList(json3);

            //get list4
            String json4 = sharedPreferences.getString(NewUIActivity.SELECTED_PRODUCTS_KEY4, "");
            List<Product> parsedList4 = parseJsonList(json4);


            //save list4 in sp
            String jsonStr4 = gson.toJson(parsedList3);
            sharedPreferences.edit().putString(NewUIActivity.SELECTED_PRODUCTS_KEY4, jsonStr4).apply();

            //save list3 in sp
            String jsonStr3 = gson.toJson(parsedList2);
            sharedPreferences.edit().putString(NewUIActivity.SELECTED_PRODUCTS_KEY3, jsonStr3).apply();

            //save list2 in sp
            String jsonStr2 = gson.toJson(productList);
            sharedPreferences.edit().putString(NewUIActivity.SELECTED_PRODUCTS_KEY2, jsonStr2).apply();

            product.setQuantity(product.getQuantity() + 1);

            //save list1 in sp
            String jsonStr1 = gson.toJson(productList);
            sharedPreferences.edit().putString(NewUIActivity.SELECTED_PRODUCTS_KEY, jsonStr1).apply();


            String j1 = sharedPreferences.getString(SELECTED_PRODUCTS_KEY, "");
            String j2 = sharedPreferences.getString(SELECTED_PRODUCTS_KEY2, "");
            String j3 = sharedPreferences.getString(SELECTED_PRODUCTS_KEY3, "");
            String j4 = sharedPreferences.getString(SELECTED_PRODUCTS_KEY4, "");


            Log.i("primary prod list ", parseJsonList(j1).toString());
            Log.i("sec prod list ", parseJsonList(j2).toString());
            Log.i("thi prod list ", parseJsonList(j3).toString());
            Log.i("four prod list ", parseJsonList(j4).toString());

            List<Product> prodList2 = parseJsonList(j2);
            int qty2=0;
            for (Product p : prodList2) {
                if(p.getName().equalsIgnoreCase(product.getName())) {
                    qty2 = p.getQuantity();
                }
            }

            List<Product> prodList3 = parseJsonList(j3);
            int qty3=0;
            for (Product p : prodList3) {
                if(p.getName().equalsIgnoreCase(product.getName())) {
                    qty3 = p.getQuantity();
                }
            }

            List<Product> prodList4 = parseJsonList(j4);
            int qty4=0;
            for (Product p : prodList4) {
                if(p.getName().equalsIgnoreCase(product.getName())) {
                    qty4 = p.getQuantity();
                }
            }



            updateMainActionButton(holder, holder.buttonMainAction, product.getQuantity(), qty2, qty3, qty4);
            onQuantityChangedCallback.accept(null); // Notify MainActivity to save
        });

        // --- Small Minus Button Listener ---
        holder.minusMainBtn.setOnClickListener(v -> {
            List<Product> secProductList = new ArrayList<>();
            secProductList.addAll(productList);
            if (product.getQuantity() > 0) {
                setSecProdList(holder, secProductList, position);

                product.setQuantity(product.getQuantity() - 1);
            } else {
                product.setQuantity(0); // Ensure it doesn't go below zero
            }
            // Clear any pending input for this product
            currentInput.setLength(0);
            holder.textCurrentQuantityInput.setVisibility(View.GONE);

            updateMainActionButton(holder, holder.buttonMainAction, product.getQuantity(), 0, 0, 0);
            onQuantityChangedCallback.accept(null); // Notify MainActivity to save
        });
    }

    public static List<Product> parseJsonList(String json) {
        List<Product> savedProducts = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<Product>>() {
            }.getType();
            savedProducts = gson.fromJson(json, type);

        }
        return savedProducts;
    }

    private void setSecProdList(ProductViewHolder holder, List<Product> secProductList, int position) {
        if (!secProductList.isEmpty())
            holder.buttonNum2.setText(String.valueOf(secProductList.get(position).getQuantity()));
        else
            holder.buttonNum2.setText(String.valueOf(0));
    }

    // Helper method to apply the quantity from grid input
    private void applyGridQuantity(@NonNull ProductViewHolder holder, Product product, StringBuilder currentInput) {
        if (currentInput.length() > 0 && !currentInput.toString().equals(".")) { // Ensure input is not empty or just a dot
            try {
                // Convert input to a float first to handle decimals, then cast to int for quantity
                float parsedQuantity = Float.parseFloat(currentInput.toString());
                int quantityToAdd = (int) parsedQuantity;

                product.setQuantity(quantityToAdd);

//                updateSharedPrefs();


            } catch (NumberFormatException e) {
                product.setQuantity(0); // Reset if invalid input
            }
        } else {
//            product.setQuantity(product.getQuantity() + 1); // Default to +1 if no input or invalid
        }
        currentInput.setLength(0); // Clear input after applying
        holder.textCurrentQuantityInput.setVisibility(View.GONE); // Hide input display
    }

    private void updateSharedPrefs() {
        List<Product> productsToSave = new ArrayList<>();
        for (Product product : productList) {
            if (product.getQuantity() > 0) {
                productsToSave.add(product);
            }
        }
        String json = gson.toJson(productsToSave);
        sharedPreferences.edit().putString(SELECTED_PRODUCTS_KEY, json).apply();
        Log.d("MainActivity", "Saved JSON  > : " + json); // Uncomment for debugging
    }


    // Helper method to update the main action button's icon
    private void updateMainActionButton(ProductViewHolder holder, ImageButton button, int quantity, int qty2, int qty3, int qty4) {
        holder.buttonNum1.setText(String.valueOf(quantity));
        holder.buttonNum2.setText(String.valueOf(qty2));
        holder.buttonNum3.setText(String.valueOf(qty3));
        holder.buttonNum4.setText(String.valueOf(qty4));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        ImageButton buttonSmallMinus; // The small minus near product name
        TextView textEditableQuantityLabel;
        TextView textCurrentQuantityInput; // Displays what's being typed on the grid
        ImageButton buttonMainAction, minusMainBtn; // The large +/- button on the right

        // Grid buttons
        Button buttonNum1, buttonNum2, buttonNum3, buttonNum4, buttonDot;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textProductName);
            buttonSmallMinus = itemView.findViewById(R.id.buttonSmallMinus);
            textEditableQuantityLabel = itemView.findViewById(R.id.textEditableQuantityLabel);
            textCurrentQuantityInput = itemView.findViewById(R.id.textCurrentQuantityInput);
            buttonMainAction = itemView.findViewById(R.id.buttonMainAction);
            minusMainBtn = itemView.findViewById(R.id.minusMainBtn);

            // Initialize Grid buttons
            buttonNum1 = itemView.findViewById(R.id.buttonNum1);
            buttonNum2 = itemView.findViewById(R.id.buttonNum2);
            buttonNum3 = itemView.findViewById(R.id.buttonNum3);
            buttonNum4 = itemView.findViewById(R.id.buttonNum4); // Assuming 4-9 are present, based on image variations
        }
    }
}