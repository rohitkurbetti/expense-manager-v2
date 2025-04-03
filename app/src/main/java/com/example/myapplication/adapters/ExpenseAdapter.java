package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.dtos.ExpenseParticularsDto;
import com.example.myapplication.fragments.ExpensesFragment;
import com.example.myapplication.utils.JsonUtils;
import com.google.gson.Gson;

import java.util.List;

public class ExpenseAdapter extends BaseAdapter {
    private Context context;
    private List<Expense> expenseList;
    private LinearLayout selectionOverlayFragExp;

    public ExpenseAdapter(Context context, List<Expense> expenseList, LinearLayout selectionOverlayFragExp) {
        this.context = context;
        this.expenseList = expenseList;
        this.selectionOverlayFragExp = selectionOverlayFragExp;
    }


    @Override
    public int getCount() {
        return expenseList.size();
    }

    @Override
    public Object getItem(int position) {
        return expenseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_expense, parent, false);
        }

        TextView txtExpenseId = convertView.findViewById(R.id.txtExpenseId);
        TextView txtExpPart = convertView.findViewById(R.id.txtExpPart);
        TextView txtExpAmount = convertView.findViewById(R.id.txtExpAmount);
        TextView txtExpCreatedDateTime = convertView.findViewById(R.id.txtExpCreatedDateTime);
//        TextView txtSales = convertView.findViewById(R.id.txtSales);
        TextView txtExpBalance = convertView.findViewById(R.id.txtExpBalance);
        ImageView expDetailsBtn = convertView.findViewById(R.id.expDetailsBtn);
        CheckBox checkBoxExpenseId = convertView.findViewById(R.id.checkBoxExpenseId);

        Expense expense = expenseList.get(position);

        txtExpenseId.setText("Expense ID: " + expense.getId());

        String expPartJson = expense.getExpensePart();

        if (JsonUtils.isValidJson(expPartJson)) {
            Gson gson = new Gson();
            ExpenseParticularsDto expenseParticularsDto = gson.fromJson(expPartJson, ExpenseParticularsDto.class);
            txtExpPart.setText(expenseParticularsDto.getExpenseParticulars());
        } else {
            txtExpPart.setText(expense.getExpensePart());
        }


        txtExpAmount.setText("Total expenses: \u20B9" + expense.getExpenseAmount());
        txtExpCreatedDateTime.setText("Created: " + expense.getExpenseDateTime());
//        txtExpCreatedDate.setText("Date: " + expense.getExpenseDate());
        txtExpBalance.setText("Balance: \u20B9" + expense.getBalance());


//        expDetailsBtn.setOnClickListener(v -> expenseDetails(itemSaleMap));

        checkBoxExpenseId.setChecked(expense.getChecked());

        checkBoxExpenseId.setOnCheckedChangeListener((v, isChecked) -> {
            if(isChecked) {
                expense.setChecked(true);
            } else {
                expense.setChecked(false);
            }

            boolean isCheckedAny = expenseList.stream().anyMatch(Expense::getChecked);

            if(isCheckedAny) {
                //show delete button
                selectionOverlayFragExp.setVisibility(View.VISIBLE);
            } else {
                //hide delete button
                selectionOverlayFragExp.setVisibility(View.GONE);
            }

            long checkedCExpensesCount = expenseList.stream().filter(Expense::getChecked).count();
            ExpensesFragment.itemSelectedTxtFragExp.setText(checkedCExpensesCount+" Items selected");
        });



        return convertView;
    }

    public void updateList(List<Expense> filteredExpenseList) {
        expenseList = filteredExpenseList;
        notifyDataSetChanged();
    }
}
