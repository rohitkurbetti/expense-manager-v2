package com.example.myapplication.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listGroupTitles; // Dates
    private Map<String, List<String>> listChildData;
    private Map<String, Long> expExpAmount;
    private Map<String, Long> expMonthAmount;

    public MyExpandableListAdapter(Context context, List<String> listGroupTitles,
                                   Map<String, List<String>> listChildData, Map<String, Long> expExpAmount, Map<String, Long> expMonthAmount) {
        this.context = context;
        this.listGroupTitles = listGroupTitles;
        this.listChildData = listChildData;
        this.expExpAmount = expExpAmount;
        this.expMonthAmount = expMonthAmount;
    }

    @Override
    public int getGroupCount() {
        return listGroupTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String group = listGroupTitles.get(groupPosition);
        return listChildData.get(group).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listGroupTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String group = listGroupTitles.get(groupPosition);
        return listChildData.get(group).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // Group (Date)
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.group_item_expense, parent, false);
        }


        TextView groupTitleTextView = convertView.findViewById(R.id.groupTitle);
        groupTitleTextView.setText(groupTitle);

        TextView groupAmount = convertView.findViewById(R.id.groupAmount);

        Long expAmountVal = expMonthAmount.get(groupTitle);
        groupAmount.setText("₹ "+expAmountVal);


        return convertView;
    }

    // Child (Expense)
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        String expense = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.child_item_expense, parent, false);


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getExpenseDetails(listGroupTitles.get(groupPosition), expense);
                }
            });

        }

        TextView particularsTextView = convertView.findViewById(R.id.childParticulars);
        TextView amountTextView = convertView.findViewById(R.id.childAmount);

        particularsTextView.setText(expense);

        long expAmt = expExpAmount.get(expense);
        List<String> expList =  listChildData.get(listGroupTitles.get(groupPosition));
        amountTextView.setText("₹ "+expAmt);

        return convertView;
    }

    private void getExpenseDetails(String month, String expDate) {

        String year = null;
        if(StringUtils.isNotBlank(month)) {
            year = month.substring(month.length()-4);

            SharedPreferences sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);
            String deviceModel = sharedPreferences.getString("model", Build.MODEL);

            String path = deviceModel+"/"+"expenses"+"/"+year+"/"+month+"/"+expDate;

            DatabaseReference expRef = FirebaseDatabase.getInstance().getReference(path);

            expRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Expense expense = dataSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setIcon(R.drawable.expense);

                        StringBuilder stringBuilder = new StringBuilder();

                        stringBuilder.append("Expense Particulars: ");
                        stringBuilder.append(expense.getExpensePart());
                        stringBuilder.append("\n");
                        stringBuilder.append("Expense Date: ");
                        stringBuilder.append(expense.getExpenseDate());
                        stringBuilder.append("\n");
                        stringBuilder.append("Expense amount: ");
                        stringBuilder.append(expense.getExpenseAmount());
                        stringBuilder.append("\n");
                        stringBuilder.append("Yesterdays balance: ");
                        stringBuilder.append(expense.getYesterdaysBalance());
                        stringBuilder.append("\n");
                        stringBuilder.append("Sales: ");
                        stringBuilder.append(expense.getSales());
                        stringBuilder.append("\n");
                        stringBuilder.append("Expense Balance: ");
                        stringBuilder.append(expense.getBalance());
                        stringBuilder.append("\n");

                        builder.setMessage(stringBuilder.toString());


                        builder.setTitle("Expense details");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });


                        AlertDialog dialog = builder.create();
                        dialog.show();


                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }


    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

