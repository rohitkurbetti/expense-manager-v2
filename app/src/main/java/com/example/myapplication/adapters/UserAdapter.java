package com.example.myapplication.adapters;

import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapterholders.CustomItem;
import com.example.myapplication.adapterholders.DataModel;
import com.example.myapplication.R;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<DataModel> usersList;
    private OnItemClickListener onItemClickListener;

    public UserAdapter(List<DataModel> usersList) {
        this.usersList = usersList;
    }

    public UserAdapter(List<DataModel> usersList, OnItemClickListener onItemClickListener) {
        this.usersList = usersList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_card, parent, false);
        return new UserViewHolder(view);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        DataModel dataModel = usersList.get(position);
        holder.name.setText(dataModel.getTitle());

        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableStringBuilder builder1 = new SpannableStringBuilder();

        List<CustomItem> itemList = dataModel.getItemList();
        List<CustomItem> otherItemsList = dataModel.getOtherItemsList();
        itemList.addAll(otherItemsList);
        char bulletSymbol='\u2022';
        AtomicInteger total = new AtomicInteger();
        itemList.forEach(i -> {
            total.addAndGet(i.getAmount());
            int randomColor = getRandomNiceColor();

            String colorText = bulletSymbol+"\t\t"+i.getName()+"  ("+new BigDecimal(i.getSliderValue()).longValue()+")";
            String colorText1 = ""+i.getAmount();

            builder.append(colorText, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder1.append(colorText1, new ForegroundColorSpan(randomColor), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n\n");
            builder1.append("\n\n");

        });


        holder.phone.setText("Total   "+total+"\n");
        holder.city.setText(builder);
        holder.designation.setText(builder1);

        holder.btnPrintInvoice.setOnClickListener(v -> onItemClickListener.onItemClick(dataModel.getId(),null));


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date();
        try {
            date = sdf.parse(dataModel.getTimestamp());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        holder.address.setText(new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(date));





    }




    private int getRandomNiceColor() {
        // Define an array of specific nice-looking colors
        int[] colors = new int[] {
                Color.rgb(129, 199, 132),  // Light Green
                Color.rgb(100, 181, 246),  // Light Blue
                Color.rgb(255, 213, 79),   // Amber
                Color.rgb(239, 83, 80),    // Light Red
                Color.rgb(186, 104, 200),  // Light Purple
                Color.rgb(255, 167, 38),   // Orange
                Color.rgb(38, 198, 218),   // Cyan
                Color.rgb(255, 112, 67),   // Deep Orange
                Color.rgb(255, 241, 118),  // Light Yellow
                Color.rgb(174, 213, 129)   // Pale Green
        };

        // Generate a random index to pick a color
        Random random = new Random();
        int randomIndex = random.nextInt(colors.length);

        // Return the randomly selected color
        return colors[randomIndex];
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String id, String date);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, phone, city, designation;
        ImageButton btnPrintInvoice;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            phone = itemView.findViewById(R.id.phone);
            city = itemView.findViewById(R.id.city);
            designation = itemView.findViewById(R.id.designation);
            btnPrintInvoice = itemView.findViewById(R.id.btnPrintInvoice);
        }
    }
}

