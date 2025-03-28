package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;

public class ExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<Expense> expenses;
    private int maxExpense; // The maximum expense value for percentage calculation

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
        this.maxExpense = calculateMaxExpense(expenses);
    }

    @Override
    public int getCount() {
        return expenses.size();
    }

    @Override
    public Object getItem(int position) {
        return expenses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        Expense expense = expenses.get(position);

        TextView expenseText = convertView.findViewById(R.id.expenseText);
        View filledBar = convertView.findViewById(R.id.filledBar);

        // Set expense text
        expenseText.setText("INR " + expense.getValue());

        // Calculate percentage fill
        int percentage = (int) ((expense.getValue() / (float) maxExpense) * 100);

        // Set width dynamically
        ViewGroup.LayoutParams params = filledBar.getLayoutParams();
        params.width = (int) (parent.getWidth() * (percentage / 100.0));
        filledBar.setLayoutParams(params);

        return convertView;
    }

    private int calculateMaxExpense(List<Expense> expenses) {
        int max = 0;
        for (Expense expense : expenses) {
            if (expense.getValue() > max) {
                max = expense.getValue();
            }
        }
        return max;
    }
}
