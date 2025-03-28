package com.example.myapplication.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.R;
import com.google.android.material.slider.Slider;

import java.util.List;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    private static final String SHARED_PREFS_FILE = "my_shared_prefs";

    private Context context;
    private List<CustomItem> itemList;
    private TextView tvSelectiveTotalText;

    public CustomAdapter(Context context, List<CustomItem> itemList, TextView tvSelectiveTotalText) {
        this.context = context;
        this.itemList = itemList;
        this.tvSelectiveTotalText = tvSelectiveTotalText;
    }

    public CustomAdapter(List<CustomItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        CustomItem item = itemList.get(position);
        return (item.hashCode());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        CustomItem item = itemList.get(position);
        holder.textView.setText(item.getName());
//        holder.checkBox.setChecked(item.isChecked());
        holder.slider.setValue(item.getSliderValue());
        holder.valueTextView.setText(String.valueOf((int)item.getSliderValue()));


//        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            item.setChecked(isChecked);
//            holder.slider.setEnabled(isChecked);
//        });

        holder.slider.addOnChangeListener((slider, value, fromUser) -> {
            item.setSliderValue((int) value);
            Double aDouble = (double) value;

            holder.valueTextView.setText(String.valueOf(Double.valueOf(aDouble*100).intValue()));

            int quantity = Double.valueOf(aDouble*100).intValue();

            int amount = quantity * getItemPrice(item.getName());

            itemList.get(position).setSliderValue(quantity);
            itemList.get(position).setAmount(amount);
            itemList.get(position).setName(item.getName());
            itemList.get(position).setChecked(item.isChecked());

            if(itemList.get(position).getAmount()>0) {
                tvSelectiveTotalText.setVisibility(View.VISIBLE);
                tvSelectiveTotalText.setText("Selected item: \n" + itemList.get(position).getName() + " : \u20B9" + itemList.get(position).getAmount());
            } else {
                tvSelectiveTotalText.setVisibility(View.GONE);
                tvSelectiveTotalText.setText(null);
            }


        });
    }

    private int getItemPrice(String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        int retrievedIntValue = sharedPreferences.getInt(name.toUpperCase(Locale.getDefault()), 0);
        if(retrievedIntValue>0){
            return retrievedIntValue;
        }
        int price = InvoiceConstants.ITEM_PRICE_MAP.getOrDefault(name.toUpperCase(Locale.getDefault()),0);
        return price;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }



    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public Slider slider;
        public TextView valueTextView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            slider = itemView.findViewById(R.id.slider);
            valueTextView = itemView.findViewById(R.id.valueTextView);
        }
    }

}
