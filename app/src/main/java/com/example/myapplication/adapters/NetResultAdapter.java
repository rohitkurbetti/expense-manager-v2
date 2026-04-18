package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.dtos.NetResult;

import java.util.List;

public class NetResultAdapter extends RecyclerView.Adapter<NetResultAdapter.NetResultViewHolder> {
    private List<NetResult> invoiceList;
    private Context context;

    public NetResultAdapter(List<NetResult> invoiceList, Context context) {
        this.invoiceList = invoiceList;
        this.context = context;
    }

    @NonNull
    @Override
    public NetResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_net_result, parent, false);
        return new NetResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NetResultAdapter.NetResultViewHolder holder, int position) {
        NetResult invoice = invoiceList.get(position);

        holder.tvMonth.setText(invoice.getMonth());
        holder.tvSales.setText(String.format("₹%.2f", invoice.getSales()));
        holder.tvExpenses.setText(String.format("₹%.2f", invoice.getExpenses()));
        holder.tvProfitLoss.setText(String.format("₹%.2f", invoice.getProfitLoss()));

        // Set different colors for profit/loss
        if (invoice.getProfitLoss() >= 0) {
            holder.tvProfitLoss.setTextColor(ContextCompat.getColor(context, R.color.profit_green));
            holder.tvProfitLabel.setTextColor(ContextCompat.getColor(context, R.color.profit_green));
        } else {
            holder.tvProfitLoss.setTextColor(ContextCompat.getColor(context, R.color.loss_red));
            holder.tvProfitLabel.setTextColor(ContextCompat.getColor(context, R.color.loss_red));
        }

        // Add click animation
        holder.itemView.setOnClickListener(v -> {
            animateClick(v);
            showInvoiceDetails(invoice);
        });
    }

    @Override
    public int getItemCount() {
        return invoiceList.size();
    }

    private void animateClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(150)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private void showInvoiceDetails(NetResult invoice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Invoice Details - " + invoice.getMonth())
                .setMessage(String.format(
                        "Sales: ₹%.2f\nExpenses: ₹%.2f\nProfit & Loss: ₹%.2f\nDate: %s",
                        invoice.getSales(), invoice.getExpenses(),
                        invoice.getProfitLoss(), invoice.getDate()
                ))
                .setPositiveButton("OK", null)
                .show();
    }

    static class NetResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonth, tvSales, tvExpenses, tvProfitLoss, tvProfitLabel;
        CardView cardView;

        public NetResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvSales = itemView.findViewById(R.id.tvSales);
            tvExpenses = itemView.findViewById(R.id.tvExpenses);
            tvProfitLoss = itemView.findViewById(R.id.tvProfitLoss);
            tvProfitLabel = itemView.findViewById(R.id.tvProfitLabel);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}