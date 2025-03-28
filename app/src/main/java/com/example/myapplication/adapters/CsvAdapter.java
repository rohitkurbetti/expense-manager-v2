package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;

import java.util.List;

public class CsvAdapter extends BaseAdapter {

    private Context context;
    private List<CustomItem> itemList;

    public CsvAdapter(Context context, List<CustomItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }


    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.csv_item, parent, false);
        }
        TextView serialNumber = convertView.findViewById(R.id.tvSerialNumber);
        TextView itemName = convertView.findViewById(R.id.tvItemName);
        ImageButton deleteButton = convertView.findViewById(R.id.btnDelete);

        CustomItem item = itemList.get(position);
        serialNumber.setText(String.valueOf(position + 1)); // Serial number starts from 1
        itemName.setText(item.getName());

        // Delete item when button is clicked
        deleteButton.setOnClickListener(v -> {
            itemList.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, item.getName()+" removed", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}
