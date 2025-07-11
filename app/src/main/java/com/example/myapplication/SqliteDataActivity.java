package com.example.myapplication;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapters.ViewPagerAdapter;
import com.example.myapplication.database.ExpenseDbHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SqliteDataActivity extends AppCompatActivity {



    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ExpenseDbHelper expenseDbHelper;
    private Menu menu;
    private TextView badge;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_data);
        expenseDbHelper = new ExpenseDbHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button on Action Bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Invoices");
                            break;
                        case 1:
                            tab.setText("Expenses");
                            break;
                    }
                }).attach();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<LocalDate> checkForMissingInvoices() {
        List<String> missingInvoicesStrList = expenseDbHelper.getMissingInvoicesParsedList();
        List<LocalDate> missingInvoiceList = new ArrayList<>();

        if(!missingInvoicesStrList.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            missingInvoicesStrList.forEach(i -> {
                missingInvoiceList.add(LocalDate.parse(i, formatter));
            });

            AtomicInteger ctr = new AtomicInteger(1);
            String message = missingInvoiceList.stream()
                    .map(date -> (ctr.getAndIncrement())+". " + date.format(formatter))
                    .collect(Collectors.joining("\n"));

            new AlertDialog.Builder(this)
                    .setTitle("Missing invoices for below dates")
                    .setIcon(R.drawable.alert_attention_exclamation_mark_security_warning_shield_svgrepo_com)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("OK", null)
                    .show();

            }
        return missingInvoiceList;
    }

    // Handle back button click
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Navigate back to the previous activity
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_section, menu);
        this.menu = menu;

        MenuItem item = menu.findItem(R.id.action_check_missing_expenses);
        item.setActionView(R.layout.notification_badge);

        View actionView = item.getActionView();
        badge = actionView.findViewById(R.id.badge);


        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(item);
            }
        });


        List<LocalDate> missingInvoiceDates = checkForMissingInvoices();

        if (missingInvoiceDates.isEmpty()) {
            toggleMenuItem(false);
        } else {
            badge.setText(String.valueOf(missingInvoiceDates.size()));
            toggleMenuItem(true);
        }


        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_check_missing_expenses) {

            List<LocalDate> missingInvoiceDates = checkForMissingInvoices();

            if (missingInvoiceDates.isEmpty()) {
                toggleMenuItem(false);
            } else {
                toggleMenuItem(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleMenuItem(boolean enable) {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_check_missing_expenses);
            if (item != null) {
                item.setVisible(enable);
                item.setIcon(R.drawable.warning);
            }
        }
    }

    private void applyUserTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("app_theme", "Theme.ExpenseUtility");

        switch (theme) {
            case "Default": setTheme(R.style.Base_Theme_MyApplication); break;
            case "Red": setTheme(R.style.AppTheme_Red); break;
            case "Blue": setTheme(R.style.AppTheme_Blue); break;
            case "Green": setTheme(R.style.AppTheme_Green); break;
            case "Purple": setTheme(R.style.AppTheme_Purple); break;
            case "Orange": setTheme(R.style.AppTheme_Orange); break;
            case "Teal": setTheme(R.style.AppTheme_Teal); break;
            case "Pink": setTheme(R.style.AppTheme_Pink); break;
            case "Cyan": setTheme(R.style.AppTheme_Cyan); break;
            case "Lime": setTheme(R.style.AppTheme_Lime); break;
            case "Brown": setTheme(R.style.AppTheme_Brown); break;
            case "Mint": setTheme(R.style.AppTheme_Mint); break;
            case "Coral": setTheme(R.style.AppTheme_Coral); break;
            case "Steel": setTheme(R.style.AppTheme_Steel); break;
            case "Lavender": setTheme(R.style.AppTheme_Lavender); break;
            case "Mustard": setTheme(R.style.AppTheme_Mustard); break;
            default: setTheme(R.style.Base_Theme_MyApplication); break;
        }
    }

}