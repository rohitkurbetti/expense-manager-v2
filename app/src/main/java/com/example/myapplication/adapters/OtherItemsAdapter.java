package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;

import java.util.List;
import java.util.Set;

public class OtherItemsAdapter extends BaseAdapter {

    private Context context;
    private List<CustomItem> otherItemList;
    public OtherItemsAdapter(Context context, List<CustomItem> otherItemList) {
        this.context = context;
        this.otherItemList = otherItemList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomItem item = (CustomItem) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(com.example.myapplication.R.layout.other_items, parent, false);
        }

        TextView itemName = convertView.findViewById(R.id.itemName);
        TextView itemRate = convertView.findViewById(R.id.itemRate);
        TextView itemQty = convertView.findViewById(R.id.itemQty);
        TextView itemAmount = convertView.findViewById(R.id.itemAmount);
        ImageView deleteOtherItemBtn = convertView.findViewById(R.id.deleteOtherItemBtn);

        itemName.setText(item.getName());
        itemRate.setText(String.valueOf("\u20B9"+item.getAmount()));
        itemQty.setText(String.valueOf(" x "+(int) item.getSliderValue()));
        itemAmount.setText(String.valueOf("\u20B9"+(int) item.getSliderValue() * item.getAmount()));

        deleteOtherItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }


    @Override
    public int getCount() {
        return otherItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return otherItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}

