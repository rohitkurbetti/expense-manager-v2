package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class CategoryItem {
    private String id;
    private String name;
    private List<CategoryItem> subCategories;
    private List<DateData> dates;
    private boolean isExpanded;
    private boolean isChecked;
    private int level;
    private boolean hasChildren;
    private boolean isDateLevel;
    private boolean isItemLevel;
    private Object userObject;

    public CategoryItem() {
        this.subCategories = new ArrayList<>();
        this.dates = new ArrayList<>();
        this.isExpanded = false; // Start collapsed
        this.isChecked = false;  // Start unchecked
        this.level = 0;
        this.hasChildren = false;
        this.isDateLevel = false;
        this.isItemLevel = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<CategoryItem> getSubCategories() { return subCategories; }
    public void setSubCategories(List<CategoryItem> subCategories) {
        this.subCategories = subCategories;
        updateHasChildren();
    }

    public List<DateData> getDates() { return dates; }
    public void setDates(List<DateData> dates) {
        this.dates = dates;
        updateHasChildren();
    }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) {
        this.isChecked = checked;
        // Only propagate to children if this is a manual set (not during initialization)
        if (subCategories != null && !subCategories.isEmpty()) {
            for (CategoryItem child : subCategories) {
                child.setChecked(checked);
            }
        }
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public boolean hasChildren() { return hasChildren; }
    public void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }

    public boolean isDateLevel() { return isDateLevel; }
    public void setDateLevel(boolean dateLevel) { isDateLevel = dateLevel; }

    public boolean isItemLevel() { return isItemLevel; }
    public void setItemLevel(boolean itemLevel) { isItemLevel = itemLevel; }

    public Object getUserObject() { return userObject; }
    public void setUserObject(Object userObject) { this.userObject = userObject; }

    private void updateHasChildren() {
        this.hasChildren = (subCategories != null && !subCategories.isEmpty()) ||
                (dates != null && !dates.isEmpty());
    }

    // Helper methods
    public boolean isLeafCategory() {
        return (subCategories == null || subCategories.isEmpty()) &&
                (dates == null || dates.isEmpty());
    }

    public void ensureSubCategories() {
        if (subCategories == null) {
            subCategories = new ArrayList<>();
        }
    }

    public void ensureDates() {
        if (dates == null) {
            dates = new ArrayList<>();
        }
    }

    public CategoryItem findCategoryById(String id) {
        if (this.id.equals(id)) {
            return this;
        }
        if (subCategories != null) {
            for (CategoryItem subCategory : subCategories) {
                CategoryItem found = subCategory.findCategoryById(id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public List<CategoryItem> getAllCheckedCategories() {
        List<CategoryItem> checked = new ArrayList<>();
        if (this.isChecked) {
            checked.add(this);
        }
        if (subCategories != null) {
            for (CategoryItem child : subCategories) {
                checked.addAll(child.getAllCheckedCategories());
            }
        }
        return checked;
    }

    // Method to uncheck children without triggering expansion changes
    public void uncheckChildrenSilently() {
        if (subCategories != null) {
            for (CategoryItem child : subCategories) {
                child.setChecked(false);
                child.uncheckChildrenSilently();
            }
        }
    }

    // New method to get the maximum depth of this category tree
    public int getMaxDepth() {
        return getMaxDepthRecursive(this, 0);
    }

    private int getMaxDepthRecursive(CategoryItem category, int currentDepth) {
        if (category == null || category.getSubCategories() == null || category.getSubCategories().isEmpty()) {
            return currentDepth;
        }

        int maxDepth = currentDepth;
        for (CategoryItem child : category.getSubCategories()) {
            int childDepth = getMaxDepthRecursive(child, currentDepth + 1);
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth;
    }

    // New method to check if this category can have more levels (up to level 4)
    public boolean canAddMoreLevels() {
        return getMaxDepth() < 4; // Maximum 4 levels supported
    }

    // New method to get all categories at a specific level
    public List<CategoryItem> getCategoriesAtLevel(int targetLevel) {
        List<CategoryItem> result = new ArrayList<>();
        getCategoriesAtLevelRecursive(this, 0, targetLevel, result);
        return result;
    }

    private void getCategoriesAtLevelRecursive(CategoryItem category, int currentLevel, int targetLevel, List<CategoryItem> result) {
        if (category == null) return;

        if (currentLevel == targetLevel) {
            result.add(category);
            return;
        }

        if (category.getSubCategories() != null) {
            for (CategoryItem child : category.getSubCategories()) {
                getCategoriesAtLevelRecursive(child, currentLevel + 1, targetLevel, result);
            }
        }
    }

    // Add this method to CategoryItem.java
    public int getCurrentLevel() {
        return this.level;
    }

    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }
}