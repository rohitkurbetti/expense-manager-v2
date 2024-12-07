package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.DataModel;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
    private List<DataModel> dataList;
    private OnItemClickListener onItemClickListener;

    private List<DataModel> dataListFull; // For filtering purpose


    public DataAdapter(List<DataModel> dataList) {
        this.dataList = dataList;
        this.dataListFull = new ArrayList<>(dataList);

    }

    public interface OnItemClickListener {
        void onItemClick(String docId);

        boolean onLongClick(String date, String id);
    }

    public DataAdapter(List<DataModel> dataList, OnItemClickListener onItemClickListener) {
        this.dataList = dataList;
        this.onItemClickListener = onItemClickListener;
        this.dataListFull = new ArrayList<>(dataList);

    }

    public void filter(String text) {
        if(dataListFull.size()==0){
            dataListFull.addAll(dataList);
        }
        dataList.clear();
        if (text.isEmpty()) {
            dataList.addAll(dataListFull);
        } else {
            text = text.toLowerCase();
            for (DataModel item : dataListFull) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    dataList.add(item);
                }
            }
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_v2, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        DataModel dataModel = dataList.get(position);
        holder.titleTextView.setText(dataModel.getTitle());
        holder.subtitleTextView.setText(dataModel.getSubtitle());
        holder.timestampTextView.setText(dataModel.getTimestamp());
//        Glide.with(holder.profileImageView.getContext()).load(dataModel.getProfileImageUrl()).into(holder.profileImageView);
        holder.itemView1.setOnClickListener(v -> onItemClickListener.onItemClick(dataModel.getDate()+"_"+dataModel.getId()));
        holder.itemView1.setOnLongClickListener(v -> onItemClickListener.onLongClick(dataModel.getDate(),dataModel.getId()));

        holder.itemView1.setBackgroundColor(dataModel.isSelected() ?
                ContextCompat.getColor(holder.itemView1.getContext(), R.color.yellow1) :
                ContextCompat.getColor(holder.itemView1.getContext(), android.R.color.primary_text_dark));

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {
        View itemView1;
        TextView titleTextView;
        TextView subtitleTextView;
        TextView timestampTextView;
        ImageView profileImageView;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView1 = itemView.findViewById(R.id.itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}

