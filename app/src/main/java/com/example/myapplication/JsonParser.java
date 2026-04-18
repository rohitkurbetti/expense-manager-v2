package com.example.myapplication;

import android.content.Context;
import android.content.res.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    private static final String UPDATED_FILE_NAME = "categories_updated.json";

    public static CategoriesResponse parseCategoriesFromJson(Context context) {
        // First try to load from updated file
        CategoriesResponse response = loadFromUpdatedFile(context);
        if (response != null && response.getCategories() != null && !response.getCategories().isEmpty()) {
            setLevelsAndPropertiesRecursive(response.getCategories(), 0);
            return response;
        }

        // If no updated file exists, load from raw resource
        try {
            Resources resources = context.getResources();
            int resId = resources.getIdentifier("categories", "raw", context.getPackageName());

            if (resId == 0) {
                return createSampleData();
            }

            InputStream inputStream = resources.openRawResource(resId);
            InputStreamReader reader = new InputStreamReader(inputStream);

            Gson gson = new GsonBuilder().create();
            response = gson.fromJson(reader, CategoriesResponse.class);

            reader.close();
            inputStream.close();

            if (response.getCategories() != null) {
                setLevelsAndPropertiesRecursive(response.getCategories(), 0);
            } else {
                response.setCategories(new ArrayList<>());
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return createSampleData();
        }
    }

    private static CategoriesResponse loadFromUpdatedFile(Context context) {
        try {
            File file = new File(context.getFilesDir(), UPDATED_FILE_NAME);
            if (!file.exists()) {
                return null;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line);
            }

            bufferedReader.close();
            reader.close();
            fis.close();

            Gson gson = new GsonBuilder().create();
            CategoriesResponse response = gson.fromJson(jsonString.toString(), CategoriesResponse.class);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveCategoriesToJson(Context context, CategoriesResponse response) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(response);

            // Save to internal storage
            File file = new File(context.getFilesDir(), UPDATED_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(json.getBytes());
            fos.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetToOriginal(Context context) {
        try {
            File file = new File(context.getFilesDir(), UPDATED_FILE_NAME);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void setLevelsAndPropertiesRecursive(List<CategoryItem> categories, int level) {
        if (categories == null) return;

        for (CategoryItem category : categories) {
            category.setLevel(level);

            boolean hasSubCategories = category.getSubCategories() != null && !category.getSubCategories().isEmpty();
            boolean hasDates = category.getDates() != null && !category.getDates().isEmpty();
            category.setHasChildren(hasSubCategories || hasDates);

            // Ensure all categories start collapsed and unchecked
            category.setExpanded(false);
            category.setChecked(false);

            if (hasSubCategories) {
                setLevelsAndPropertiesRecursive(category.getSubCategories(), level + 1);
            }
        }
    }

    private static CategoriesResponse createSampleData() {
        CategoriesResponse response = new CategoriesResponse();
        List<CategoryItem> categories = new ArrayList<>();

        // Create sample electronics category
        CategoryItem electronics = new CategoryItem();
        electronics.setId("electronics");
        electronics.setName("Electronics");
        electronics.setExpanded(false); // Start collapsed
        electronics.setChecked(false);  // Start unchecked
        electronics.setSubCategories(new ArrayList<>());

        // Create sample smartphones subcategory
        CategoryItem smartphones = new CategoryItem();
        smartphones.setId("smartphones");
        smartphones.setName("Smartphones");
        smartphones.setExpanded(false); // Start collapsed
        smartphones.setChecked(false);  // Start unchecked
        smartphones.setSubCategories(new ArrayList<>());

        electronics.getSubCategories().add(smartphones);
        categories.add(electronics);

        response.setCategories(categories);
        setLevelsAndPropertiesRecursive(categories, 0);
        return response;
    }
}