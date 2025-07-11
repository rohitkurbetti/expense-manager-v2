package com.example.myapplication.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.constants.InvoiceConstants;
import com.example.myapplication.R;
import com.google.android.material.slider.Slider;

import org.apache.commons.lang3.StringUtils;

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

        holder.valueTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogToResizeSlider(item.getName());
            }

            private void showAlertDialogToResizeSlider(String itemName) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Set value ["+itemName+"]");
                builder.setCancelable(false);

                builder.setIcon(R.drawable.list_tick_svgrepo_com);

                View view = LayoutInflater.from(context).inflate(R.layout.resize_slider_row, null, false);

                EditText etSliderQty = view.findViewById(R.id.resizeSliderQtyEditText);
                etSliderQty.requestFocus();
                builder.setView(view);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(StringUtils.isNotBlank(etSliderQty.getText())) {
                            float fVal = Float.parseFloat(String.valueOf(etSliderQty.getText()));

                            if(fVal>0.0f && fVal<10000.0f) {
                                holder.slider.setValueTo(fVal / 100);
                                Toast.makeText(context, "["+itemName+"] max qty = "+(int) fVal, Toast.LENGTH_SHORT).show();

                                animateHighlight(holder.itemView);


                            } else {
                                Toast.makeText(context, "Invalid range for ["+itemName+"] ", Toast.LENGTH_SHORT).show();
                                holder.slider.setValueTo((float) 30 / 100);

                            }

                        }


                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etSliderQty.setText(String.valueOf(30));
                        holder.slider.setValueTo((float) 30 / 100);
                        dialog.dismiss();
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        if(StringUtils.containsIgnoreCase(holder.textView.getText(), "Lassi")) {
            holder.slider.setValueTo(1.00f);
        }

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

    private void animateHighlight(View itemView) {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(300); // duration for one flash
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(4); // total flashes = 2
        itemView.startAnimation(animation);
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
