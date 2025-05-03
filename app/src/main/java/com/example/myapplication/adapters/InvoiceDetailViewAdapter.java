package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.myapplication.R;
import com.example.myapplication.dtos.Item;

import java.util.List;

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
        TextView titleTextView = convertView.findViewById(R.id.textViewParticulars);
        TextView amountTextView = convertView.findViewById(R.id.textViewAmount);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewCategory);

        textViewItemSrNo.setText((position+1) + ".");
        titleTextView.setText(currentItem.getItemName());
        amountTextView.setText("\u20B9"+currentItem.getItemAmount());
        descriptionTextView.setText("x"+currentItem.getItemQty());


        return convertView;
    }

}
