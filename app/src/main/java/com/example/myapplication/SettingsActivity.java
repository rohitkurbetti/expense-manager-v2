package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.commons.lang3.StringUtils;

public class SettingsActivity extends AppCompatActivity {
    private Spinner periodSpinner;
    private EditText invoiceFragmentLoadingDelay;
    private Switch enableMinusBtn,showSlider,enablePrettyPrintExport,enableCloudExport;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences(MainActivity.SHARED_PREFS_FILE, MODE_PRIVATE);

        periodSpinner = findViewById(R.id.periodSpinner);
        enableMinusBtn = findViewById(R.id.enableMinusBtn);
        showSlider = findViewById(R.id.showSlider);
        invoiceFragmentLoadingDelay = findViewById(R.id.invoiceFragmentLoadingDelay);
        enablePrettyPrintExport = findViewById(R.id.enablePrettyPrintExport);
        enableCloudExport = findViewById(R.id.enableCloudExport);

        // Dropdown options
        String[] periods = {
                "Last 30 days",
                "Last 60 days",
                "Last 90 days",
                "Last 6 months",
                "Last 1 Year"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                periods
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapter);

        String selectedPeriod = prefs.getString("selectedPeriod", "Last 30 days"); // default
        Log.d("MainActivity", "Current selectedPeriod: " + selectedPeriod);
        int spinnerPosition = adapter.getPosition(selectedPeriod);

        if (spinnerPosition >= 0) {
            periodSpinner.setSelection(spinnerPosition);
        }

        // Handle selection
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (!selected.equals(selectedPeriod)) {
                    // You can save selection in SharedPreferences for later use
                    prefs.edit().putString("selectedPeriod", selected).apply();

                    // Show refresh dialog
                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle("Refresh Required")
                            .setMessage("Changes will take effect after refreshing the dashboard. Refresh now?")
                            .setPositiveButton("Refresh", (dialog, which) -> {
                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish(); // close SettingsActivity
                            })
                            .setNegativeButton("Later", null)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        boolean enableMinus = prefs.getBoolean("enableMinus", true);
        enableMinusBtn.setChecked(enableMinus);

        boolean enableCloudExportVal = prefs.getBoolean("enableCloudExport", false);

        if(enableCloudExportVal) {
            enableCloudExport.setChecked(enableCloudExportVal);
            enablePrettyPrintExport.setVisibility(View.VISIBLE);
        }


        enableCloudExport.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                prefs.edit().putBoolean("enableCloudExport", true).apply();
                enablePrettyPrintExport.setVisibility(View.VISIBLE);
            } else {
                prefs.edit().putBoolean("enableCloudExport", false).apply();
                enablePrettyPrintExport.setVisibility(View.GONE);
            }
        });

        enableMinusBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    prefs.edit().putBoolean("enableMinus", true).apply();
                } else {
                    prefs.edit().putBoolean("enableMinus", false).apply();
                }
                // Show refresh dialog
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Refresh Required")
                        .setMessage("Changes will take effect after refreshing the dashboard. Refresh now?")
                        .setPositiveButton("Refresh", (dialog, which) -> {
                            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // close SettingsActivity
                        })
                        .setNegativeButton("Later", null)
                        .show();
            }
        });


        boolean showSliderVal = prefs.getBoolean("showSlider", true);
        showSlider.setChecked(showSliderVal);

        boolean enablePrettyPrintExportVal = prefs.getBoolean("enablePrettyPrintExport", false);
        enablePrettyPrintExport.setChecked(enablePrettyPrintExportVal);

        enablePrettyPrintExport.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    prefs.edit().putBoolean("enablePrettyPrintExport", true).apply();
                } else {
                    prefs.edit().putBoolean("enablePrettyPrintExport", false).apply();
                }
            }
        });

        int loadingDelay = prefs.getInt("invoiceFragmentLoadingDelay", 1000);
        invoiceFragmentLoadingDelay.setText(String.valueOf(loadingDelay));
        invoiceFragmentLoadingDelay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String loadingDelay = invoiceFragmentLoadingDelay.getText().toString();
                if(StringUtils.isNotEmpty(loadingDelay)) {
                    int delay = Integer.parseInt(loadingDelay);
                    prefs.edit().putInt("invoiceFragmentLoadingDelay", delay).apply();
                    Toast.makeText(SettingsActivity.this, "Time Delay set: "+delay, Toast.LENGTH_SHORT).show();
                }
            }
        });




        showSlider.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    prefs.edit().putBoolean("showSlider", true).apply();
                } else {
                    prefs.edit().putBoolean("showSlider", false).apply();
                }
                // Show refresh dialog
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Refresh Required")
                        .setMessage("Changes will take effect after refreshing the dashboard. Refresh now?")
                        .setPositiveButton("Refresh", (dialog, which) -> {
                            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // close SettingsActivity
                        })
                        .setNegativeButton("Later", null)
                        .show();
            }
        });


    }
}