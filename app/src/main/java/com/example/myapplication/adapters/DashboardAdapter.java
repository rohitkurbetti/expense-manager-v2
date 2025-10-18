package com.example.myapplication.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.dtos.ItemModel;
import com.example.myapplication.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    private Context context;
    private List<ItemModel> itemList;
    private int maxQuantity;

    public DashboardAdapter(Context context, List<ItemModel> itemList) {
        this.context = context;
        this.itemList = itemList;

        // Find max quantity for scaling progress bars
        maxQuantity = 0;
        for (ItemModel item : itemList) {
            if (item.getQuantity() > maxQuantity) {
                maxQuantity = item.getQuantity();
            }
        }
    }

    public void updateData(List<ItemModel> newData) {
        // Find max quantity for scaling progress bars
        maxQuantity = 0;
        for (ItemModel item : newData) {
            if (item.getQuantity() > maxQuantity) {
                maxQuantity = item.getQuantity();
            }
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardAdapter.ViewHolder holder, int position) {
        ItemModel item = itemList.get(position);

        holder.tvItemName.setText(item.getItemName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Scale progress bar based on max sold
        int targetProgress = (int) (((double) item.getQuantity() / maxQuantity) * 100);

        // Animate progress bar filling smoothly
        ObjectAnimator animator = ObjectAnimator.ofInt(holder.progressBar, "progress", 0, targetProgress);
        animator.setDuration(1200); // animation duration in ms
        animator.setInterpolator(new DecelerateInterpolator()); // smooth effect
        animator.start();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQuantity;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
