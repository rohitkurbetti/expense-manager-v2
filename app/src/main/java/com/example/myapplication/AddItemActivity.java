package com.example.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private Spinner spinnerMainCategory, spinnerSubCategory, spinnerSubSubCategory,spinnerLevel4Category;
    private TextInputEditText etItemName, etBrand, etPrice, etDescription, etImageUrl, etProductLinks, etRemarks;
    private Button btnSelectDate, btnSave, btnCancel, btnBrowseImage;
    private TextView tvSelectedDate;

    private List<CategoryItem> categories;
    private CategoryItem selectedMainCategory, selectedSubCategory, selectedSubSubCategory, selectedLevel4Category;
    private Calendar selectedDate;

    private static final int PICK_IMAGE_REQUEST = 1;
    private String currentImagePath;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        initializeViews();
        loadCategories();
        setupSpinners();
        setupClickListeners();
    }

    private void initializeViews() {
        spinnerMainCategory = findViewById(R.id.spinnerMainCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        spinnerSubSubCategory = findViewById(R.id.spinnerSubSubCategory);
        spinnerLevel4Category = findViewById(R.id.spinnerLevel4Category); // Add this line

        etItemName = findViewById(R.id.etItemName);
        etBrand = findViewById(R.id.etBrand);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etImageUrl = findViewById(R.id.etImageUrl);
        etProductLinks = findViewById(R.id.etProductLinks);
        etRemarks = findViewById(R.id.etRemarks);

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

//        etImageUrl.setOnClickListener(v -> openGallery());

        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        selectedDate = Calendar.getInstance();
        updateDateDisplay();

        // Initialize all category variables
        selectedMainCategory = null;
        selectedSubCategory = null;
        selectedSubSubCategory = null;
        selectedLevel4Category = null;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadCategories() {
        CategoriesResponse response = JsonParser.parseCategoriesFromJson(this);
        categories = response.getCategories();
    }

    private void setupSpinners() {
        // Main Categories
        List<String> mainCategoryNames = new ArrayList<>();
        mainCategoryNames.add("Select Main Category");
        for (CategoryItem category : categories) {
            mainCategoryNames.add(category.getName());
        }

        ArrayAdapter<String> mainCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mainCategoryNames);
        mainCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMainCategory.setAdapter(mainCategoryAdapter);

        // Sub Categories (initially empty)
        ArrayAdapter<String> subCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subCategoryAdapter);

        // Sub Sub Categories (initially empty)
        ArrayAdapter<String> subSubCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        subSubCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubSubCategory.setAdapter(subSubCategoryAdapter);

        // Level 4 Categories (initially empty)
        ArrayAdapter<String> level4CategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        level4CategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel4Category.setAdapter(level4CategoryAdapter);

        // Spinner listeners
        spinnerMainCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedMainCategory = categories.get(position - 1);
                    updateSubCategories();
                } else {
                    selectedMainCategory = null;
                    clearSubCategories();
                }
                updateLevelIndicator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMainCategory = null;
                clearSubCategories();
                updateLevelIndicator();
            }
        });

        spinnerSubCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && selectedMainCategory != null) {
                    selectedSubCategory = selectedMainCategory.getSubCategories().get(position - 1);
                    updateSubSubCategories();
                } else {
                    selectedSubCategory = null;
                    clearSubSubCategories();
                }
                updateLevelIndicator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubCategory = null;
                clearSubSubCategories();
                updateLevelIndicator();
            }
        });

        spinnerSubSubCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && selectedSubCategory != null) {
                    selectedSubSubCategory = selectedSubCategory.getSubCategories().get(position - 1);
                    updateLevel4Categories();
                } else {
                    selectedSubSubCategory = null;
                    clearLevel4Categories();
                }
                updateLevelIndicator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubSubCategory = null;
                clearLevel4Categories();
                updateLevelIndicator();
            }
        });

        spinnerLevel4Category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && selectedSubSubCategory != null) {
                    selectedLevel4Category = selectedSubSubCategory.getSubCategories().get(position - 1);
                } else {
                    selectedLevel4Category = null;
                }
                updateLevelIndicator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLevel4Category = null;
                updateLevelIndicator();
            }
        });
    }


    private void clearLevel4Categories() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel4Category.setAdapter(adapter);
        selectedLevel4Category = null;
    }

    private void updateLevel4Categories() {
        if (selectedSubSubCategory == null) return;

        List<String> level4CategoryNames = new ArrayList<>();
        level4CategoryNames.add("Select Level 4 Category");

        if (selectedSubSubCategory.getSubCategories() != null) {
            for (CategoryItem level4Category : selectedSubSubCategory.getSubCategories()) {
                level4CategoryNames.add(level4Category.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, level4CategoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel4Category.setAdapter(adapter);
    }

    private void updateLevelIndicator() {
        TextView tvSelectedLevel = findViewById(R.id.tvSelectedLevel);
        if (tvSelectedLevel != null) {
            int level = 0;
            if (selectedMainCategory != null) level = 1;
            if (selectedSubCategory != null) level = 2;
            if (selectedSubSubCategory != null) level = 3;
            if (selectedLevel4Category != null) level = 4;

            tvSelectedLevel.setText("Selected Level: " + level);
        }
    }

    private void updateSubCategories() {
        if (selectedMainCategory == null) return;

        List<String> subCategoryNames = new ArrayList<>();
        subCategoryNames.add("Select Sub Category");
        for (CategoryItem subCategory : selectedMainCategory.getSubCategories()) {
            subCategoryNames.add(subCategory.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subCategoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(adapter);

        // Clear lower level spinners
        clearSubSubCategories();

    }

    private void updateSubSubCategories() {
        if (selectedSubCategory == null) return;

        List<String> subSubCategoryNames = new ArrayList<>();
        subSubCategoryNames.add("Select Sub Sub Category");

        if (selectedSubCategory.getSubCategories() != null) {
            for (CategoryItem subSubCategory : selectedSubCategory.getSubCategories()) {
                subSubCategoryNames.add(subSubCategory.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subSubCategoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubSubCategory.setAdapter(adapter);

        // Clear level 4 spinner
        clearLevel4Categories();
    }

    private void clearSubCategories() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory.setAdapter(adapter);
        clearSubSubCategories();
    }

    private void clearSubSubCategories() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubSubCategory.setAdapter(adapter);
        clearLevel4Categories();
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveItem());

        btnCancel.setOnClickListener(v -> finish());

//        btnBrowseImage.setOnClickListener(v -> openGallery());

        // Also allow clicking on the image URL field to browse
        etImageUrl.setOnClickListener(v -> openGallery());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    updateDateDisplay();
                }, year, month, day);

        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        tvSelectedDate.setText("Selected: " + dateFormat.format(selectedDate.getTime()));
    }

    private void saveItem() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new item
            ItemDetail newItem = createNewItem();

            // Add item to selected category
            addItemToCategory(newItem);

            // Save updated JSON using JsonParser
            CategoriesResponse response = new CategoriesResponse();
            response.setCategories(categories);

            boolean success = JsonParser.saveCategoriesToJson(this, response);

            if (success) {
                Toast.makeText(this, "Item saved successfully!", Toast.LENGTH_SHORT).show();

                // Return to main activity with success result
                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh_needed", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error saving item to storage", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error saving item: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean validateInput() {
        if (selectedMainCategory == null) {
            Toast.makeText(this, "Please select a main category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(etItemName.getText())) {
            etItemName.setError("Item name is required");
            return false;
        }

        if (TextUtils.isEmpty(etBrand.getText())) {
            etBrand.setError("Brand is required");
            return false;
        }

        if (TextUtils.isEmpty(etPrice.getText())) {
            etPrice.setError("Price is required");
            return false;
        }

        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Description is required");
            return false;
        }

        try {
            double price = Double.parseDouble(etPrice.getText().toString());
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            return false;
        }

        return true;
    }

    private ItemDetail createNewItem() {
        ItemDetail item = new ItemDetail();
        item.setItemName(etItemName.getText().toString().trim());
        item.setItemBrand(etBrand.getText().toString().trim());
        item.setItemPrice(Double.parseDouble(etPrice.getText().toString()));
        item.setDescription(etDescription.getText().toString().trim());
        item.setPurchaseDateTime(dateFormat.format(selectedDate.getTime()) + "T12:00:00");

        if (!TextUtils.isEmpty(etImageUrl.getText())) {
            item.setImageUrl(etImageUrl.getText().toString().trim());
        } else {
            item.setImageUrl("https://example.com/images/default.jpg");
        }

        if (!TextUtils.isEmpty(etProductLinks.getText())) {
            item.setItemLinks(etProductLinks.getText().toString().trim());
        } else {
            item.setItemLinks("https://example.com/products/" +
                    etItemName.getText().toString().trim().toLowerCase().replace(" ", "-"));
        }

        item.setItemDoc("https://example.com/docs/" +
                etItemName.getText().toString().trim().toLowerCase().replace(" ", "-"));

        if (!TextUtils.isEmpty(etRemarks.getText())) {
            item.setRemarks(etRemarks.getText().toString().trim());
        } else {
            item.setRemarks("Added via mobile app");
        }

        return item;
    }

    private void addItemToCategory(ItemDetail newItem) {
        // Determine the target category (level 4 has highest priority, then level 3, etc.)
        CategoryItem targetCategory = selectedLevel4Category != null ? selectedLevel4Category :
                selectedSubSubCategory != null ? selectedSubSubCategory :
                        selectedSubCategory != null ? selectedSubCategory :
                                selectedMainCategory;

        // Get or create dates list
        if (targetCategory.getDates() == null) {
            targetCategory.setDates(new ArrayList<>());
        }

        String dateString = dateFormat.format(selectedDate.getTime());
        DateData targetDate = null;

        // Find existing date or create new one
        for (DateData date : targetCategory.getDates()) {
            if (date.getDate().equals(dateString)) {
                targetDate = date;
                break;
            }
        }

        if (targetDate == null) {
            targetDate = new DateData();
            targetDate.setDate(dateString);
            targetDate.setItems(new ArrayList<>());
            targetCategory.getDates().add(targetDate);
        }

        // Add item to date
        if (targetDate.getItems() == null) {
            targetDate.setItems(new ArrayList<>());
        }
        targetDate.getItems().add(newItem);

        String categoryPath = getCategoryPath(targetCategory);
        Toast.makeText(this,
                "Item added to " + categoryPath + " for " + dateString,
                Toast.LENGTH_LONG).show();
    }

    private String getCategoryPath(CategoryItem category) {
        if (category == null) return "Unknown Category";

        // Build the category path by traversing up the hierarchy
        List<String> pathSegments = new ArrayList<>();
        CategoryItem current = category;

        // We'll build the path by checking if this category exists in our hierarchy
        // This is a simplified approach - you might want to implement a more robust path builder
        if (selectedLevel4Category != null && selectedLevel4Category.equals(category)) {
            pathSegments.add(selectedLevel4Category.getName());
            if (selectedSubSubCategory != null) pathSegments.add(0, selectedSubSubCategory.getName());
            if (selectedSubCategory != null) pathSegments.add(0, selectedSubCategory.getName());
            if (selectedMainCategory != null) pathSegments.add(0, selectedMainCategory.getName());
        } else if (selectedSubSubCategory != null && selectedSubSubCategory.equals(category)) {
            pathSegments.add(selectedSubSubCategory.getName());
            if (selectedSubCategory != null) pathSegments.add(0, selectedSubCategory.getName());
            if (selectedMainCategory != null) pathSegments.add(0, selectedMainCategory.getName());
        } else if (selectedSubCategory != null && selectedSubCategory.equals(category)) {
            pathSegments.add(selectedSubCategory.getName());
            if (selectedMainCategory != null) pathSegments.add(0, selectedMainCategory.getName());
        } else if (selectedMainCategory != null && selectedMainCategory.equals(category)) {
            pathSegments.add(selectedMainCategory.getName());
        } else {
            return category.getName();
        }

        return TextUtils.join(" → ", pathSegments);
    }

    // Handle result from AddCategoryActivity and Image Picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Refresh categories and spinners
            loadCategories();
            setupSpinners();
            Toast.makeText(this, "New category added! Please select it from the list.", Toast.LENGTH_LONG).show();
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Take persistable URI permission
            try {
                getContentResolver().takePersistableUriPermission(imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                Log.w("ImagePicker", "Could not take persistable permission, continuing anyway");
            }

            // Try to get actual path
            String filePath = getFilePathFromUri(imageUri);

            if (filePath != null && new File(filePath).exists()) {
                etImageUrl.setText(filePath);
                currentImagePath = filePath;
                Toast.makeText(this, "Image selected: " + new File(filePath).getName(), Toast.LENGTH_LONG).show();
            } else {
                // If path resolution fails, use the copy method
                Toast.makeText(this, "Copying image to app storage...", Toast.LENGTH_SHORT).show();
                copyFileToInternalStorage(imageUri);
            }
        }
    }

    private String getFilePathFromUri(Uri uri) {
        try {
            // For Documents
            if (DocumentsContract.isDocumentUri(this, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);

                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    String[] split = docId.split(":");
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        String path = Environment.getExternalStorageDirectory() + "/" + (split.length > 1 ? split[1] : "");
                        if (new File(path).exists()) {
                            return path;
                        }
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    // Handle different formats of docId
                    if (docId.startsWith("raw:")) {
                        // raw file path
                        String path = docId.substring(4);
                        if (new File(path).exists()) {
                            return path;
                        }
                    } else if (docId.startsWith("msf:")) {
                        // MSF format - use alternative method
                        return getFilePathFromDownloadsProvider(uri);
                    } else {
                        try {
                            // Try numeric ID (traditional method)
                            Uri contentUri = ContentUris.withAppendedId(
                                    Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                            String path = getDataColumn(contentUri, null, null);
                            if (path != null && new File(path).exists()) {
                                return path;
                            }
                        } catch (NumberFormatException e) {
                            // If numeric parsing fails, try alternative method
                            return getFilePathFromDownloadsProvider(uri);
                        }
                    }
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    String[] split = docId.split(":");
                    String type = split[0];
                    Uri contentUri = null;
                    switch (type) {
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }
                    if (contentUri != null && split.length > 1) {
                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{split[1]};
                        String path = getDataColumn(contentUri, selection, selectionArgs);
                        if (path != null && new File(path).exists()) {
                            return path;
                        }
                    }
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Try multiple approaches for content URIs
                String result = getDataColumn(uri, null, null);
                if (result != null && new File(result).exists()) {
                    return result;
                }

                // Fallback: try to get display name and construct path
                return getFilePathFromContentUri(uri);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                String path = uri.getPath();
                if (path != null && new File(path).exists()) {
                    return path;
                }
            }
        } catch (Exception e) {
            Log.e("FilePath", "Error getting file path from URI: " + uri, e);
        }

        // Final fallback
        return getFilePathByCopyingTemp(uri);
    }

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};

        try {
            cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("FilePath", "Error in getDataColumn", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    // Alternative method for DownloadsProvider
    private String getFilePathFromDownloadsProvider(Uri uri) {
        try {
            // Try to get the file path using DISPLAY_NAME and RELATIVE_PATH
            String[] projection = {
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.RELATIVE_PATH
            };

            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME);
                    int pathIndex = cursor.getColumnIndex(MediaStore.Downloads.RELATIVE_PATH);

                    String displayName = nameIndex != -1 ? cursor.getString(nameIndex) : null;
                    String relativePath = pathIndex != -1 ? cursor.getString(pathIndex) : null;

                    if (displayName != null) {
                        if (relativePath != null) {
                            // Construct the full path
                            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            String fullPath = new File(downloadsDir, relativePath + File.separator + displayName).getAbsolutePath();
                            if (new File(fullPath).exists()) {
                                return fullPath;
                            }
                        }

                        // Fallback: just use Downloads directory
                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        String fallbackPath = new File(downloadsDir, displayName).getAbsolutePath();
                        if (new File(fallbackPath).exists()) {
                            return fallbackPath;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("FilePath", "Error in getFilePathFromDownloadsProvider", e);
        }
        return null;
    }

    // Method for content URIs
    private String getFilePathFromContentUri(Uri contentUri) {
        try {
            // Try to get display name
            String displayName = null;
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};

            try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex);
                    }
                }
            }

            // If we have a display name, try to construct a path
            if (displayName != null) {
                // Check various possible locations
                String[] possiblePaths = {
                        Environment.getExternalStorageDirectory().getPath() + "/Download/" + displayName,
                        Environment.getExternalStorageDirectory().getPath() + "/Downloads/" + displayName,
                        Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + displayName,
                        Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + displayName,
                        Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + displayName
                };

                for (String path : possiblePaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        return path;
                    }
                }
            }

        } catch (Exception e) {
            Log.e("FilePath", "Error in getFilePathFromContentUri", e);
        }
        return null;
    }

    // Last resort: copy to temp file and return that path
    private String getFilePathByCopyingTemp(Uri uri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "temp_" + timeStamp;
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // Create directory if it doesn't exist
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File tempFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            return tempFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("FilePath", "Error copying file to temp", e);
            return null;
        }
    }

    private void copyFileToInternalStorage(Uri uri) {
        try {
            // Create a file in app's private storage (no permissions needed)
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "item_image_" + timeStamp + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Create directory if it doesn't exist
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, imageFileName);

            // Copy the file
            try (InputStream inputStream = getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(imageFile)) {

                byte[] buffer = new byte[4096]; // Larger buffer for better performance
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            // Get the actual path
            String actualPath = imageFile.getAbsolutePath();
            etImageUrl.setText(actualPath);
            currentImagePath = actualPath;
//            storeImagePathInJSON(actualPath);

            Toast.makeText(this, "Image saved to app storage: " + imageFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error copying image: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // Last resort: just use the URI string
            String uriString = uri.toString();
            etImageUrl.setText(uriString);
//            storeImagePathInJSON(uriString);
            Toast.makeText(this, "Using URI instead of file path", Toast.LENGTH_LONG).show();
        }
    }

    // Helper methods to check URI type
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private void storeImagePathInJSON(String imagePath) {
        try {
            JSONObject itemJson = new JSONObject();
            itemJson.put("imagePath", imagePath);

            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                itemJson.put("imageSize", imageFile.length());
                itemJson.put("imageName", imageFile.getName());
                itemJson.put("imageAbsolutePath", imageFile.getAbsolutePath());
                itemJson.put("imageCanonicalPath", imageFile.getCanonicalPath());
            }

            // Save to SharedPreferences or your preferred storage
            saveItemToStorage(itemJson);

        } catch (Exception e) {
            Log.e("ImagePath", "Error storing image path in JSON", e);
        }
    }

    private void saveItemToStorage(JSONObject itemJson) {
        SharedPreferences prefs = getSharedPreferences("ItemData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("currentItem", itemJson.toString());
        editor.apply();

        Log.d("ImagePath", "Stored image path: " + itemJson.toString());
    }
}