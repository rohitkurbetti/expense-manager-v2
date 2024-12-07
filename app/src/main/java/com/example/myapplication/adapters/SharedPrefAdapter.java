package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.SharedPrefItem;

import java.util.List;

public class SharedPrefAdapter extends RecyclerView.Adapter<SharedPrefAdapter.SharedPrefViewHolder>{
    private Integer val=0;
    private List<SharedPrefItem> sharedPrefItemList;

    public SharedPrefAdapter(List<SharedPrefItem> sharedPrefItemList) {
        this.sharedPrefItemList = sharedPrefItemList;
    }

    @NonNull
    @Override
    public SharedPrefAdapter.SharedPrefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_pref_item, parent, false);
        return new SharedPrefViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedPrefAdapter.SharedPrefViewHolder holder, int position) {
        SharedPrefItem sharedPrefItem = sharedPrefItemList.get(position);

        holder.detailTitle.setText(""+sharedPrefItem.getItemName());
        holder.currVal.setText(""+sharedPrefItem.getItemValue());

        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int vall = Integer.parseInt(holder.currVal.getText()+"");
                if(vall <= 0) {
                    holder.currVal.setText("0");
                } else {
                    vall= vall - 1;
                    holder.currVal.setText(String.valueOf(vall));
                }
                sharedPrefItem.setItemValue(vall);
            }
        });

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int vall = Integer.parseInt(holder.currVal.getText()+"");
                vall= vall + 1;
                holder.currVal.setText(String.valueOf(vall));
                sharedPrefItem.setItemValue(vall);
            }
        });



    }

    @Override
    public int getItemCount() {
        return sharedPrefItemList.size();
    }

    public class SharedPrefViewHolder extends RecyclerView.ViewHolder {

        Button minusButton,plusButton;
        TextView currVal,detailTitle;

        public SharedPrefViewHolder(@NonNull View itemView) {
            super(itemView);
            detailTitle = itemView.findViewById(R.id.detail_title);
            minusButton = itemView.findViewById(R.id.minusButton);
            currVal = itemView.findViewById(R.id.currVal);
            plusButton = itemView.findViewById(R.id.plusButton);
        }
    }
}
