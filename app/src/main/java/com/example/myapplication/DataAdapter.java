package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
    private List<DataModel> dataList;
    private OnItemClickListener onItemClickListener;

    public DataAdapter(List<DataModel> dataList) {
        this.dataList = dataList;
    }

    public interface OnItemClickListener {
        void onItemClick(String docId);

        boolean onLongClick(String date, String id);
    }

    public DataAdapter(List<DataModel> dataList, OnItemClickListener onItemClickListener) {
        this.dataList = dataList;
        this.onItemClickListener = onItemClickListener;
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

