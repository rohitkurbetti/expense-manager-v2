package com.example.myapplication.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.dtos.ExpenseParticularsDto;
import com.example.myapplication.dtos.Item;
import com.example.myapplication.fragments.ExpensesFragment;
import com.example.myapplication.utils.JsonUtils;
import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        TextView txtSales = convertView.findViewById(R.id.txtSales);
        TextView txtExpBalance = convertView.findViewById(R.id.txtExpBalance);
        ImageView expDetailsBtn = convertView.findViewById(R.id.expDetailsBtn);
        LinearLayout expenseDetailsLayout = convertView.findViewById(R.id.expenseDetailsLayout);
        CheckBox checkBoxExpenseId = convertView.findViewById(R.id.checkBoxExpenseId);



        Expense expense = expenseList.get(position);

        expDetailsBtn.setOnClickListener(v -> expenseDetails(expense));

        if(JsonUtils.isValidJson(expense.getExpensePart())) {
            expenseDetailsLayout.setVisibility(View.VISIBLE);
        } else {
            expenseDetailsLayout.setVisibility(View.GONE);
        }

        txtExpenseId.setText("Expense ID: " + expense.getId());

        String expPartJson = expense.getExpensePart();

        if (StringUtils.isNotBlank(expPartJson) && JsonUtils.isValidJson(expPartJson)) {
            Gson gson = new Gson();
            ExpenseParticularsDto expenseParticularsDto = gson.fromJson(expPartJson, ExpenseParticularsDto.class);
            txtExpPart.setText(expenseParticularsDto.getExpenseParticulars());
        } else {
            txtExpPart.setText(expense.getExpensePart());
        }


        txtExpAmount.setText("Total expenses: \u20B9" + expense.getExpenseAmount());
        txtExpCreatedDateTime.setText("Created: " + expense.getExpenseDateTime());
        txtSales.setText("Sales: \u20B9" + expense.getSales());
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

    private void expenseDetails(Expense expense) {
        String expPartJsonStr = expense.getExpensePart();

        Gson gson = new Gson();
        ExpenseParticularsDto expenseParticularsDto = gson.fromJson(expPartJsonStr, ExpenseParticularsDto.class);

        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.nested_items_popup, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        LinearLayout subTitleLinearLayout = dialogView.findViewById(R.id.subTitleLinearLayout);
        ListView popupListView = dialogView.findViewById(R.id.popupListView);
        TextView popupTitle = dialogView.findViewById(R.id.popupTitle);
        TextView tvItemDatePopup = dialogView.findViewById(R.id.tvItemDatePopup);
        TextView tvItemTotalPopup = dialogView.findViewById(R.id.tvItemTotalPopup);

        popupTitle.setText("Expense Details Breakdown");
        subTitleLinearLayout.setVisibility(View.GONE);
        tvItemDatePopup.setVisibility(View.GONE);
        tvItemTotalPopup.setVisibility(View.GONE);

        Button btnClose = dialogView.findViewById(R.id.btnClosePopup);

        List<Item> items = getItemListParsed(expenseParticularsDto.getExpensePriceMap());

        InvoiceDetailViewAdapter adapter = new InvoiceDetailViewAdapter(context, items);
        popupListView.setAdapter(adapter);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Close button handler
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private List<Item> getItemListParsed(Map<String, Integer> expensePriceMap) {
        List<Item> items = new ArrayList<>();

        expensePriceMap.forEach((k,v) -> {
            items.add(new Item(k, 1, v));
        });
        return items;
    }

    public void updateList(List<Expense> filteredExpenseList) {
        expenseList = filteredExpenseList;
        notifyDataSetChanged();
    }
}
