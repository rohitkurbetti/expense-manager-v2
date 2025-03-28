package com.example.myapplication;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.MainAdapter;
import com.example.myapplication.dtos.Day;
import com.example.myapplication.dtos.DtoJsonEntity;
import com.example.myapplication.dtos.Month;
import com.example.myapplication.dtos.Year;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudSummary extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("invoices");
    DatabaseReference usersRef;
    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    static CopyOnWriteArrayList<Month> monthList;

    public static Map<String, List<DtoJsonEntity>> map = new HashMap<>();
    public static Map<String, Map<String, List<DtoJsonEntity>>> monthMap = new HashMap<>();
    public static Map<String, Map<String, Map<String, List<DtoJsonEntity>>>> yearMap = new HashMap<>();
    private MainAdapter mainAdapter;
    private static AppCompatSpinner yearSpinner;
    private CopyOnWriteArrayList<Year> yearList;
    private TextView yearTotal;
    private static TextView noDataCloudTextView;
    private static ImageView noDataCloudImageView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_summary);
        recyclerView = findViewById(R.id.recyclerview);
        yearSpinner = findViewById(R.id.yearSpinner);
        yearTotal = findViewById(R.id.yearTotal);
        noDataCloudTextView = findViewById(R.id.noDataCloudTextView);
        noDataCloudImageView = findViewById(R.id.noDataCloudImageView);
        progressDialog = new ProgressDialog(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        monthList = new CopyOnWriteArrayList<>();

        loadSpinner(yearSpinner);

        fetchFromFirebase(map);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedYear = parent.getItemAtPosition(position).toString();

                fetchFromFirebase(map);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle no selection
            }
        });




    }


    private void loadSpinner(AppCompatSpinner yearSpinner) {
        // Get the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Generate a list of years (e.g., from 1900 to current year)
        List<String> years = new ArrayList<>();
        for (int year = 2022; year <= currentYear+3; year++) {
            years.add(String.valueOf(year));
        }

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                years
        );

        // Set the layout for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attach the adapter to the Spinner
        yearSpinner.setAdapter(adapter);

        // Optionally, set the current year as the selected item
        yearSpinner.setSelection(adapter.getPosition(String.valueOf(currentYear)));
    }

    private void fetchFromFirebase(Map<String, List<DtoJsonEntity>> map) {
        progressDialog.setMessage("Fetching from cloud database");
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            private String key;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();

                Long expAmt = 0L;
                Long expAmtMonth = 0L;
                    yearList = new CopyOnWriteArrayList<>();
                monthList = new CopyOnWriteArrayList<>();
                for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {

                    Year year = new Year();

                    for(DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                        expAmtMonth =0L;
                        List<Day> dayList = new ArrayList<>();
                        Month month = new Month();
                        for(DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                            Day day = new Day();
                            key = daySnapshot.getKey();
                            expAmt=0L;
                            List<DtoJsonEntity> tList = new ArrayList<>();
                            for(DataSnapshot subDay : daySnapshot.getChildren()) {
                                String userId = subDay.getKey();
                                DtoJsonEntity dtoJsonEntity = subDay.getValue(DtoJsonEntity.class);
                                expAmt += dtoJsonEntity.getTotal();
                                tList.add(dtoJsonEntity);

                            }
//                            nestedItems1.add(new NestedItem(d2.getKey(), "\u20B9"+expAmt, String.format("(%.2f%%)", ((double) expAmt/2000)*100),expAmt));
                            expAmtMonth += expAmt;
                            map.put(key, tList);
                            day.setDayName(key);
                            day.setDayTotal(expAmt);
                            day.setDtoJsonEntityList(tList);
                            dayList.add(day);
                        }
                        monthMap.put(monthSnapshot.getKey(), map);
                        month.setMonthName(monthSnapshot.getKey());
                        month.setMonthTotal(expAmtMonth);
                        month.setDayList(dayList);
                        month.setExpanded(false);
                        monthList.add(month);

                    }
                    yearMap.put(yearSnapshot.getKey(), monthMap);
                    year.setYearName(yearSnapshot.getKey());
                    year.setMonthList(monthList);
                    yearList.add(year);
                }

                String spinnerMonth = yearSpinner.getSelectedItem().toString();

                for (Month y: monthList) {
                    if(!y.getMonthName().contains(spinnerMonth)) {
                        monthList.remove(y);
                    }
                }
                int totalYear = monthList.stream().map(month -> month.getMonthTotal()).mapToInt(n -> n.intValue()).sum();
                yearTotal.setText("Total  \u20B9"+totalYear);
//                System.err.println("Total year: "+totalYear);
                // Set the adapter
                mainAdapter = new MainAdapter(CloudSummary.this,  monthList);
                recyclerView.setAdapter(mainAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                System.err.println("Error: " + databaseError.getMessage());
            }
        });

    }


}