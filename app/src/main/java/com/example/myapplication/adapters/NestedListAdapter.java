package com.example.myapplication.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;

import java.util.List;

public class NestedListAdapter extends BaseAdapter {

    private Context context;
    private List<CustomItem> nestedItemList;
    private ListView nestedListView;
    SharedPreferences sharedPreferences;
    public NestedListAdapter(Context context, List<CustomItem> nestedItemList, ListView nestedListView) {
        this.context = context;
        this.nestedItemList = nestedItemList;
        this.nestedListView = nestedListView;
        sharedPreferences = context.getSharedPreferences("my_shared_prefs", Context.MODE_PRIVATE);

    }



    @Override
    public int getCount() {
        return nestedItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return nestedItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_firebase, parent, false);
        }

        TextView nestedItemTitle = convertView.findViewById(R.id.nestedItemTitle);
        TextView nestedItemExpense = convertView.findViewById(R.id.nestedItemExpense);
        TextView nestedItemExpensePer = convertView.findViewById(R.id.nestedItemExpensePer);
        TextView nestedItemExpenseAmount = convertView.findViewById(R.id.nestedItemExpenseAmount);
        ImageButton btnDeleteDateEntry = convertView.findViewById(R.id.btnDeleteDateEntry);
//        CheckBox chkBoxDay = convertView.findViewById(R.id.chkBoxDay);

//        NestedItem nestedItem = nestedItemList.get(position);
        nestedItemTitle.setText(nestedItemList.get(position).getName());
        nestedItemExpense.setText("("+(int) nestedItemList.get(position).getSliderValue()+")");
        nestedItemExpensePer.setText(String.valueOf(nestedItemList.get(position).getAmount()));

        int itemPrice = sharedPreferences.getInt(nestedItemList.get(position).getName().toUpperCase(), 0);

        nestedItemExpenseAmount.setText("\u20B9"+itemPrice * (int) nestedItemList.get(position).getSliderValue());

//        // Handle button click event
//        convertView.setOnClickListener(v -> {
//
//            List<ExpenseItem> nestedItemList = RealtimeFirebaseActivity.map.get(nestedItem.getSubItemName());
//
//            populateNestedItemsPopup(nestedItemList, nestedItem.getSubItemName(), nestedItem.getSubItemExpenseTotalAmount());
//
//
//        });

//        chkBoxDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked) {
//                    btnDeleteDateEntry.setVisibility(View.VISIBLE);
//                } else {
//                    btnDeleteDateEntry.setVisibility(View.GONE);
//                }
//            }
//        });

        btnDeleteDateEntry.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                nestedItemList.remove(position);


//                setListViewHeightBasedOnChildren(nestedListView, parent);
                notifyDataSetChanged();
                // Reference to the Firebase Realtime Database
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference databaseReference = database.getReference("expenses");

//                String childPath = "";
//                String expDate = nestedItem.getSubItemName();
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                LocalDate date = LocalDate.parse(expDate, formatter);

//                int year = date.getYear();

//                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM"); // "MMM" gives abbreviated month
//                String month = monthFormatter.format(date);

//                childPath = "/"+year +"/"+(month+"-"+year)+"/"+expDate;
//                databaseReference.child(childPath).removeValue()
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(context, "Entries deleted on "+expDate, Toast.LENGTH_LONG).show();
//                                // Navigate back to MainActivity after successful deletion
//                                Intent intent = new Intent(context, MainActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Clear the activity stack
//                                context.startActivity(intent);
//                            } else {
//                                Toast.makeText(context, "Failed to delete entry", Toast.LENGTH_SHORT).show();
//                            }
//                        });


            }
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(nestedListView, (ViewGroup) nestedListView.getParent());
    }

    private void setListViewHeightBasedOnChildren(View listView, ViewGroup parent) {
        ListAdapter listAdapter = this.nestedListView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, parent);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = this.nestedListView.getLayoutParams();
        params.height = totalHeight + (this.nestedListView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}
