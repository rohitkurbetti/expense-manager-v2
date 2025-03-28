package com.example.myapplication.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.myapplication.R;
import com.example.myapplication.dtos.DtoJsonEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NestedItemAdapter extends ArrayAdapter<DtoJsonEntity> {

    private final Context context;
    private final List<DtoJsonEntity> items;
    private final List<Boolean> checkedStates; // Track the checkbox states


    public NestedItemAdapter(Context context, List<DtoJsonEntity> items) {
        super(context, R.layout.nested_item_popup_layout, items);
        this.context = context;
        this.items = items;

        this.checkedStates = new ArrayList<>(items.size());

        // Initialize all checkboxes as unchecked
        for (int i = 0; i < items.size(); i++) {
            checkedStates.add(false);
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.nested_item_popup_layout, parent, false);
        }

        DtoJsonEntity currentItem = items.get(position);

        CheckBox delNestedItemCheckBox = convertView.findViewById(R.id.delNestedItemCheckBox);
        ImageView iconViewNested = convertView.findViewById(R.id.iconViewNested);
        TextView titleTextView = convertView.findViewById(R.id.textViewParticulars);
        TextView amountTextView = convertView.findViewById(R.id.textViewAmount);
        TextView descriptionTextView = convertView.findViewById(R.id.textViewCategory);

//        iconViewNested.setImageDrawable(context.getDrawable(currentItem.getId()));
        titleTextView.setText(currentItem.getName());
        amountTextView.setText(String.valueOf("\u20B9"+currentItem.getTotal()));
        descriptionTextView.setText("x"+currentItem.getQty());

//        delNestedItemCheckBox.setChecked(checkedStates.get(position));

//        delNestedItemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                currentItem.setChecked(isChecked);
//
//
//                Boolean isChecekd= items.stream().filter(i -> i.getChecked().equals(Boolean.TRUE)).findAny().isPresent();
//
//                if(isChecekd) {
//                    NestedListAdapter.btnDeletePopup.setVisibility(View.VISIBLE);
//                } else  {
//                    NestedListAdapter.btnDeletePopup.setVisibility(View.GONE);
//                }
//
//            }
//        });


        return convertView;
    }

}
