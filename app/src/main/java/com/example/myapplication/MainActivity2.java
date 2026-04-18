package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.adapters.CategoryAdapter;

import java.io.File;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "MainActivity";
    private ListView listView;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Log.d(TAG, "Layout set successfully");
        initializeViews();
    }

    private void initializeViews() {
        Log.d(TAG, "Starting initializeViews");

        listView = findViewById(R.id.listViewC);

        if (listView == null) {
            Log.e(TAG, "ListView is NULL with ID: listView");
            Toast.makeText(this, "ListView not found! Check layout file.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "ListView found successfully");

        // Check if we have updated data
        File updatedFile = new File(getFilesDir(), "categories_updated.json");
        if (updatedFile.exists()) {
            Toast.makeText(this, "Loading updated data with your additions", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Loading original data", Toast.LENGTH_SHORT).show();
        }

        // Load data from JSON (automatically handles updated vs original)
        CategoriesResponse response = JsonParser.parseCategoriesFromJson(this);
        List<CategoryItem> categoryList = response.getCategories();

        setAllCategoriesCollapsed(categoryList);

        Log.d(TAG, "Loaded " + categoryList.size() + " main categories");

        // Initialize adapter
        adapter = new CategoryAdapter(this, categoryList, this);

        // Set adapter
        listView.setAdapter(adapter);
        Log.d(TAG, "Adapter set successfully");

        setupButtons();
    }

    private void setupButtons() {
        Button btnExpandAll = findViewById(R.id.btnExpandAll);
        Button btnCollapseAll = findViewById(R.id.btnCollapseAll);
        Button btnShowSelected = findViewById(R.id.btnShowSelected);
        Button btnAddItem = findViewById(R.id.btnAddItem);
        Button btnResetData = findViewById(R.id.btnResetData);

        if (btnExpandAll != null) {
            btnExpandAll.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.expandAll();
                    Toast.makeText(MainActivity2.this, "All categories expanded and checked", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Expand All clicked - All categories expanded and checked");
                }
            });
        }

        if (btnCollapseAll != null) {
            btnCollapseAll.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.collapseAll();
                    Toast.makeText(MainActivity2.this, "All categories collapsed and unchecked", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Collapse All clicked - All categories collapsed and unchecked");
                }
            });
        }

        if (btnShowSelected != null) {
            btnShowSelected.setOnClickListener(v -> {
                if (adapter != null) {
                    showSelectedItems();
                }
            });
        }

        if (btnAddItem != null) {
            btnAddItem.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity2.this, AddItemActivity.class);
                startActivityForResult(intent, 1);
            });
        }

        if (btnResetData != null) {
            btnResetData.setOnClickListener(v -> {
                showResetConfirmationDialog();
            });
        }

        Button btnAddCategory = findViewById(R.id.btnAddCategory);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity2.this, AddCategoryActivity.class);
                startActivityForResult(intent, 2);
            });
        }

        Log.d(TAG, "Buttons setup completed");
    }

    private void showResetConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reset Data")
                .setMessage("This will delete all added items and restore the original data. Continue?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    resetToOriginalData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetToOriginalData() {
        boolean success = JsonParser.resetToOriginal(this);
        if (success) {
            refreshData();
            Toast.makeText(this, "Data reset to original successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error resetting data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSelectedItems() {
        List<DisplayItem> checkedDisplayItems = adapter.getCheckedItems();
        List<CategoryItem> allCheckedCategories = adapter.getAllCheckedCategories();
        List<String> checkedPaths = adapter.getAllCheckedCategoriesWithPath();

        if (checkedDisplayItems.isEmpty() && allCheckedCategories.isEmpty()) {
            Toast.makeText(this, "No categories selected", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("SELECTED CATEGORIES\n\n");

        if (!checkedPaths.isEmpty()) {
            message.append("Full Paths (").append(checkedPaths.size()).append("):\n");
            for (int i = 0; i < Math.min(checkedPaths.size(), 8); i++) {
                message.append("• ").append(checkedPaths.get(i)).append("\n");
            }
            if (checkedPaths.size() > 8) {
                message.append("... and ").append(checkedPaths.size() - 8).append(" more\n\n");
            } else {
                message.append("\n");
            }
        }

        if (!allCheckedCategories.isEmpty()) {
            message.append("Individual Categories (").append(allCheckedCategories.size()).append("):\n");
            for (int i = 0; i < Math.min(allCheckedCategories.size(), 5); i++) {
                CategoryItem category = allCheckedCategories.get(i);
                message.append("• ").append(category.getName());
                if (category.hasChildren()) {
                    message.append(" (has children)");
                }
                message.append("\n");
            }
            if (allCheckedCategories.size() > 5) {
                message.append("... and ").append(allCheckedCategories.size() - 5).append(" more\n\n");
            } else {
                message.append("\n");
            }
        }

        if (!checkedDisplayItems.isEmpty()) {
            message.append("Display Items (").append(checkedDisplayItems.size()).append("):\n");
            for (int i = 0; i < Math.min(checkedDisplayItems.size(), 3); i++) {
                DisplayItem item = checkedDisplayItems.get(i);
                message.append("• ").append(item.getDisplayName()).append("\n");
            }
            if (checkedDisplayItems.size() > 3) {
                message.append("... and ").append(checkedDisplayItems.size() - 3).append(" more");
            }
        }

        Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();

        Log.d(TAG, "Show Selected clicked - " + allCheckedCategories.size() + " categories selected");
        Log.d(TAG, "Selected paths: " + checkedPaths);
    }

    @Override
    public void onCategoryClick(DisplayItem item) {
        String type = "";
        switch (item.getType()) {
            case DisplayItem.TYPE_CATEGORY: type = "Category"; break;
            case DisplayItem.TYPE_DATE: type = "Date"; break;
            case DisplayItem.TYPE_ITEM: type = "Item"; break;
        }
        String message = "Clicked: " + item.getDisplayName() + " (" + type + ")";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message);
    }

    @Override
    public void onCategoryChecked(DisplayItem item, boolean isChecked) {
        String action = isChecked ? "Checked" : "Unchecked";
        String message = action + ": " + item.getDisplayName();

        // Update the actual category in our data
        if (item.getType() == DisplayItem.TYPE_CATEGORY) {
            CategoryItem category = item.getCategory();
            category.setChecked(isChecked);

            Log.d(TAG, message + " - ID: " + category.getId());

            // Show appropriate toast messages
            if (isChecked && item.hasChildren()) {
                Toast.makeText(this, message + " - Auto-expanding", Toast.LENGTH_SHORT).show();
            } else if (!isChecked && item.hasChildren()) {
                Toast.makeText(this, message + " - Auto-collapsing", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemDetailClick(ItemDetail itemDetail) {
        // Show item details dialog
        ItemDetailsDialog dialog = new ItemDetailsDialog(this, itemDetail, this);
        dialog.show();

        String message = "Showing details for: " + itemDetail.getItemName();
        Log.d(TAG, message);
        Toast.makeText(this, "Showing item details", Toast.LENGTH_SHORT).show();
    }

    // Handle result when returning from AddItemActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1 || requestCode == 2) && resultCode == Activity.RESULT_OK) {
            boolean refreshNeeded = data.getBooleanExtra("refresh_needed", false);
            if (refreshNeeded) {
                refreshData();
            }
        }
    }

    private void refreshData() {
        // Load data from JSON (will automatically load updated version if exists)
        CategoriesResponse response = JsonParser.parseCategoriesFromJson(this);
        List<CategoryItem> categoryList = response.getCategories();

        setAllCategoriesCollapsed(categoryList);


        // Update adapter with new data
        adapter = new CategoryAdapter(this, categoryList, this);
        listView.setAdapter(adapter);

        Toast.makeText(this, "Data refreshed with new items!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Data refreshed - " + categoryList.size() + " main categories loaded");
    }

    private void setAllCategoriesCollapsed(List<CategoryItem> categories) {
        for (CategoryItem category : categories) {
            category.setExpanded(false); // Collapse all categories
            category.setChecked(false);  // Uncheck all categories

            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                setAllCategoriesCollapsed(category.getSubCategories());
            }
        }
    }
}