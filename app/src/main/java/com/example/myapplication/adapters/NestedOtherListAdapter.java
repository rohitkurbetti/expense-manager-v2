package com.example.myapplication.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.example.myapplication.adapterholders.CustomItem;

import java.util.List;

public class NestedOtherListAdapter extends BaseAdapter {

    private Context context;
    private List<CustomItem> nestedOtherItemList;
    private ListView nestedOtherListView;
    private CheckBox checkBox;
    private TextView bannerOtherItems;

    public NestedOtherListAdapter(Context context, List<CustomItem> nestedOtherItemList, ListView nestedOtherListView, CheckBox checkBox, TextView bannerOtherItems) {
        this.context = context;
        this.nestedOtherItemList = nestedOtherItemList;
        this.nestedOtherListView = nestedOtherListView;
        this.checkBox = checkBox;
        this.bannerOtherItems = bannerOtherItems;
    }

    @Override
    public int getCount() {
        return nestedOtherItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return nestedOtherItemList.get(position);
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

        nestedItemTitle.setText(nestedOtherItemList.get(position).getName());
        nestedItemExpense.setText("("+(int) nestedOtherItemList.get(position).getSliderValue()+")");
        nestedItemExpensePer.setText(String.valueOf(nestedOtherItemList.get(position).getAmount()));
        nestedItemExpenseAmount.setText("\u20B9"+nestedOtherItemList.get(position).getAmount());

//        // Handle button click event
//        convertView.setOnClickListener(v -> {
//
//            List<ExpenseItem> nestedItemList = RealtimeFirebaseActivity.map.get(nestedItem.getSubItemName());
//
//            populateNestedItemsPopup(nestedItemList, nestedItem.getSubItemName(), nestedItem.getSubItemExpenseTotalAmount());
//
//
//        });


        btnDeleteDateEntry.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                nestedOtherItemList.remove(position);

                if(nestedOtherItemList.size()==0){
                    bannerOtherItems.setVisibility(View.GONE);
                    if(checkBox != null){
                        checkBox.setChecked(false);
                    }
                }

                notifyDataSetChanged();
//                setListViewHeightBasedOnChildren(nestedOtherListView, parent);

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

    private void setListViewHeightBasedOnChildren(View listView, ViewGroup parent) {
        ListAdapter listAdapter = this.nestedOtherListView.getAdapter();
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

        ViewGroup.LayoutParams params = this.nestedOtherListView.getLayoutParams();
        params.height = totalHeight + (this.nestedOtherListView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(nestedOtherListView, (ViewGroup) nestedOtherListView.getParent());
    }

}
