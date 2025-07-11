package com.example.myapplication.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.myapplication.R;
import com.example.myapplication.dtos.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class InvoiceDetailViewAdapter extends ArrayAdapter<Item> {

    private final Context context;
    private final List<Item> items;

    public InvoiceDetailViewAdapter(Context context, List<Item> items) {
        super(context, R.layout.nested_item_popup_layout, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nested_item_popup_layout, parent, false);
        }

        Item currentItem = items.get(position);

        TextView textViewItemSrNo = convertView.findViewById(R.id.textViewItemSrNo);
        TextView iconCircle = convertView.findViewById(R.id.iconCircle);
        TextView titleTextView = convertView.findViewById(R.id.textViewParticulars);
        TextView amountTextView = convertView.findViewById(R.id.textViewAmount);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewCategory);

        textViewItemSrNo.setText((position+1) + ".");
        titleTextView.setText(currentItem.getItemName());
        amountTextView.setText("\u20B9"+currentItem.getItemAmount());
        descriptionTextView.setText("x"+currentItem.getItemQty());

        String name = currentItem.getItemName();

        if (name != null && !name.isEmpty()) {
            iconCircle.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Create circular shape with random color
        GradientDrawable circleDrawable = new GradientDrawable();
        circleDrawable.setShape(GradientDrawable.OVAL);
        circleDrawable.setSize(6,6);
        circleDrawable.setColor(getRandomMaterialColor());
        iconCircle.setBackground(circleDrawable);


        return convertView;
    }

    private int getRandomMaterialColor() {
        Random random = new Random();
        List<Integer> colorList = Arrays.asList(
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#3F51B5"), // Indigo
                Color.parseColor("#03A9F4"), // Light Blue
                Color.parseColor("#009688"), // Teal
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#795548"), // Brown
                Color.parseColor("#607D8B")  // Blue Grey
        );
        return colorList.get(random.nextInt(colorList.size()));
    }

}
