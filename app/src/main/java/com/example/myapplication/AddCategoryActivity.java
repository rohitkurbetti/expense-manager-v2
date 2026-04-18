package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddCategoryActivity extends AppCompatActivity {

    private TextInputEditText etCategoryName, etCategoryId;
    private RadioGroup radioGroupExpansion;
    private CheckBox cbAddSampleItems;
    private Button btnSelectParent, btnSaveCategory, btnCancel;
    private TextView tvSelectedParent;

    private List<CategoryItem> categories;
    private CategoryItem selectedParentCategory;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        initializeViews();
        loadCategories();
        setupClickListeners();


    }

    private void initializeViews() {
        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryId = findViewById(R.id.etCategoryId);
        radioGroupExpansion = findViewById(R.id.radioGroupExpansion);
        cbAddSampleItems = findViewById(R.id.cbAddSampleItems);
        btnSelectParent = findViewById(R.id.btnSelectParent);
        btnSaveCategory = findViewById(R.id.btnSaveCategory);
        btnCancel = findViewById(R.id.btnCancel);
        tvSelectedParent = findViewById(R.id.tvSelectedParent);

        selectedParentCategory = null; // Root level by default
    }

    private void loadCategories() {
        CategoriesResponse response = JsonParser.parseCategoriesFromJson(this);
        categories = response.getCategories();
    }

    private void setupClickListeners() {
        btnSelectParent.setOnClickListener(v -> showParentCategoryDialog());

        btnSaveCategory.setOnClickListener(v -> saveCategory());

        btnCancel.setOnClickListener(v -> finish());

        // Auto-generate ID when name changes
        etCategoryName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && TextUtils.isEmpty(etCategoryId.getText())) {
                generateCategoryId();
            }
        });
    }

    private void generateCategoryId() {
        String categoryName = etCategoryName.getText().toString().trim();
        if (!TextUtils.isEmpty(categoryName)) {
            String generatedId = categoryName.toLowerCase()
                    .replace(" ", "_")
                    .replace("&", "and")
                    .replace("-", "_")
                    .replaceAll("[^a-z0-9_]", "");
            etCategoryId.setText(generatedId);
        }
    }

    private void showParentCategoryDialog() {
        List<String> categoryOptions = new ArrayList<>();
        categoryOptions.add("Root Level (Main Category)");

        // Build category hierarchy for display
        buildCategoryOptions(categories, "", categoryOptions);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Parent Category")
                .setItems(categoryOptions.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        // Root level selected
                        selectedParentCategory = null;
                        tvSelectedParent.setText("Root Level (Main Category)");
                    } else {
                        // Find the selected category
                        String selectedPath = categoryOptions.get(which);
                        selectedParentCategory = findCategoryByPath(selectedPath);
                        if (selectedParentCategory != null) {
                            tvSelectedParent.setText(selectedPath);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void buildCategoryOptions(List<CategoryItem> categories, String parentPath, List<String> options) {
        for (CategoryItem category : categories) {
            String currentPath = parentPath.isEmpty() ? category.getName() : parentPath + " → " + category.getName();
            options.add(currentPath);

            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                buildCategoryOptions(category.getSubCategories(), currentPath, options);
            }
        }
    }

    private CategoryItem findCategoryByPath(String path) {
        return findCategoryByPathRecursive(categories, path, "");
    }

    private CategoryItem findCategoryByPathRecursive(List<CategoryItem> categories, String targetPath, String currentPath) {
        for (CategoryItem category : categories) {
            String categoryPath = currentPath.isEmpty() ? category.getName() : currentPath + " → " + category.getName();

            if (categoryPath.equals(targetPath)) {
                return category;
            }

            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                CategoryItem found = findCategoryByPathRecursive(category.getSubCategories(), targetPath, categoryPath);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void saveCategory() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new category
            CategoryItem newCategory = createNewCategory();

            // Add category to the selected parent or root level
            addCategoryToParent(newCategory);

            // Add sample items if requested
            if (cbAddSampleItems.isChecked()) {
                addSampleItems(newCategory);
            }

            // Save updated JSON
            CategoriesResponse response = new CategoriesResponse();
            response.setCategories(categories);

            boolean success = JsonParser.saveCategoriesToJson(this, response);

            if (success) {
                Toast.makeText(this, "Category saved successfully!", Toast.LENGTH_SHORT).show();

                // Return to main activity with success result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh_needed", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error saving category to storage", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error saving category: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etCategoryName.getText())) {
            etCategoryName.setError("Category name is required");
            return false;
        }

        if (TextUtils.isEmpty(etCategoryId.getText())) {
            etCategoryId.setError("Category ID is required");
            return false;
        }

        // Check if category ID already exists
        String newCategoryId = etCategoryId.getText().toString().trim();
        if (isCategoryIdExists(newCategoryId)) {
            etCategoryId.setError("Category ID already exists");
            return false;
        }

        return true;
    }

    private boolean isCategoryIdExists(String categoryId) {
        return checkCategoryIdExistsRecursive(categories, categoryId);
    }

    private boolean checkCategoryIdExistsRecursive(List<CategoryItem> categories, String targetId) {
        for (CategoryItem category : categories) {
            if (category.getId().equals(targetId)) {
                return true;
            }
            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                if (checkCategoryIdExistsRecursive(category.getSubCategories(), targetId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private CategoryItem createNewCategory() {
        CategoryItem category = new CategoryItem();
        category.setId(etCategoryId.getText().toString().trim());
        category.setName(etCategoryName.getText().toString().trim());

        // Set expansion state
        int selectedRadioId = radioGroupExpansion.getCheckedRadioButtonId();
        boolean isExpanded = selectedRadioId == R.id.radioExpanded;
        category.setExpanded(isExpanded);
        category.setChecked(isExpanded); // Auto-check if expanded

        // Initialize empty lists
        category.setSubCategories(new ArrayList<>());
        category.setDates(new ArrayList<>());

        return category;
    }

    private void addCategoryToParent(CategoryItem newCategory) {
        if (selectedParentCategory == null) {
            // Add to root level
            categories.add(newCategory);
            Toast.makeText(this, "Category added to root level", Toast.LENGTH_LONG).show();
        } else {
            // Add to selected parent
            selectedParentCategory.ensureSubCategories();
            selectedParentCategory.getSubCategories().add(newCategory);
            Toast.makeText(this,
                    "Category added under: " + selectedParentCategory.getName(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void addSampleItems(CategoryItem category) {
        category.ensureDates();

        // Create sample date data
        DateData dateData = new DateData();
        dateData.setDate(dateFormat.format(Calendar.getInstance().getTime()));

        // Create sample items
        List<ItemDetail> sampleItems = new ArrayList<>();

        ItemDetail sampleItem1 = new ItemDetail();
        sampleItem1.setItemName("Sample Item 1");
        sampleItem1.setItemBrand("Sample Brand");
        sampleItem1.setItemPrice(99.99);
        sampleItem1.setPurchaseDateTime(dateData.getDate() + "T12:00:00");
        sampleItem1.setImageUrl("https://example.com/images/sample1.jpg");
        sampleItem1.setDescription("This is a sample item for " + category.getName());
        sampleItem1.setItemLinks("https://example.com/products/sample1");
        sampleItem1.setItemDoc("https://example.com/docs/sample1");
        sampleItem1.setRemarks("Sample item added automatically");
        sampleItems.add(sampleItem1);

        ItemDetail sampleItem2 = new ItemDetail();
        sampleItem2.setItemName("Sample Item 2");
        sampleItem2.setItemBrand("Sample Brand");
        sampleItem2.setItemPrice(149.99);
        sampleItem2.setPurchaseDateTime(dateData.getDate() + "T14:30:00");
        sampleItem2.setImageUrl("https://example.com/images/sample2.jpg");
        sampleItem2.setDescription("Another sample item for " + category.getName());
        sampleItem2.setItemLinks("https://example.com/products/sample2");
        sampleItem2.setItemDoc("https://example.com/docs/sample2");
        sampleItem2.setRemarks("Second sample item");
        sampleItems.add(sampleItem2);

        dateData.setItems(sampleItems);
        category.getDates().add(dateData);

        Toast.makeText(this, "2 sample items added with current date", Toast.LENGTH_SHORT).show();
    }
}