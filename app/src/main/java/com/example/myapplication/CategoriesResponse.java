package com.example.myapplication;

import java.util.List;

public class CategoriesResponse {
    private List<CategoryItem> categories;

    public List<CategoryItem> getCategories() { return categories; }
    public void setCategories(List<CategoryItem> categories) { this.categories = categories; }

    // New method to get maximum depth across all categories
    public int getMaxDepth() {
        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        int maxDepth = 0;
        for (CategoryItem category : categories) {
            maxDepth = Math.max(maxDepth, category.getMaxDepth());
        }
        return maxDepth;
    }

    // New method to check if we can add more levels
    public boolean canAddMoreLevels() {
        return getMaxDepth() < 4; // Maximum 4 levels supported
    }

    // New method to find category by ID across all levels
    public CategoryItem findCategoryById(String id) {
        if (categories == null) return null;

        for (CategoryItem category : categories) {
            CategoryItem found = category.findCategoryById(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}