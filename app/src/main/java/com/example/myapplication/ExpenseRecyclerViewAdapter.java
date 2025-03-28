package com.example.myapplication;


import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.google.type.DateTime;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpenseRecyclerViewAdapter extends RecyclerView.Adapter<ExpenseRecyclerViewAdapter.MainViewHolder> {

    private Context context;
    private List<ExpenseRecyclerView> mainItemList;

    public ExpenseRecyclerViewAdapter(Context context, List<ExpenseRecyclerView> mainItemList) {
        this.context = context;
        this.mainItemList = mainItemList;
    }

    @NonNull
    @Override
    public ExpenseRecyclerViewAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_recycler_view, parent, false);
        return new ExpenseRecyclerViewAdapter.MainViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ExpenseRecyclerViewAdapter.MainViewHolder holder, int position) {
        ExpenseRecyclerView mainItem = mainItemList.get(position);
        String date = mainItem.getExpenseDate();
        LocalDate localDateFmtted = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String month = localDateFmtted.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase();

        String dateFmtted = localDateFmtted.getDayOfMonth()+"-"+ month +"-"+localDateFmtted.getYear();

        holder.expenseDate.setText(dateFmtted);
        holder.sales.setText(String.valueOf("Sales: \u20B9"+mainItem.getSales()));
        holder.expenseAmount.setText(String.valueOf("\u20B9 "+mainItem.getExpenseAmount()));


    }



    @Override
    public int getItemCount() {
        return mainItemList.size();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView expenseDate,sales,expenseAmount;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseDate = itemView.findViewById(R.id.expenseDate);
            sales = itemView.findViewById(R.id.sales);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
        }
    }
}


