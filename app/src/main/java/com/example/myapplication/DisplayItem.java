package com.example.myapplication;

public class DisplayItem {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_DATE = 1;
    public static final int TYPE_ITEM = 2;

    private CategoryItem category;
    private DateData dateData;
    private ItemDetail itemDetail;
    private int type;
    private int level;
    private boolean isChecked;
    private String id;
    private boolean isExpanded;

    public DisplayItem(CategoryItem category, int level, int type) {
        this.category = category;
        this.level = level;
        this.type = type;
        this.id = category.getId();
        this.isChecked = category.isChecked();
        this.isExpanded = category.isExpanded();
    }

    public DisplayItem(DateData dateData, int level, int type) {
        this.dateData = dateData;
        this.level = level;
        this.type = type;
        this.id = dateData.getDate();
        this.isChecked = false;
        this.isExpanded = false;
    }

    public DisplayItem(ItemDetail itemDetail, int level, int type) {
        this.itemDetail = itemDetail;
        this.level = level;
        this.type = type;
        this.id = "item_" + itemDetail.getItemName();
        this.isChecked = false;
        this.isExpanded = false;
    }

    public String getDisplayName() {
        switch (type) {
            case TYPE_CATEGORY:
                return getCategoryDisplayName();
            case TYPE_DATE:
                return getDateDisplayName();
            case TYPE_ITEM:
                return getItemDisplayName();
            default:
                return "";
        }
    }

    private String getCategoryDisplayName() {
        String name = category.getName();
        // Add level indicator for better visualization
        String levelIndicator = " (L" + (category.getLevel() + 1) + ")";

        // Add child count if available
        String childInfo = "";
        if (category.hasChildren()) {
            int subCategoryCount = category.getSubCategories() != null ? category.getSubCategories().size() : 0;
            int dateCount = category.getDates() != null ? category.getDates().size() : 0;
            childInfo = " [" + subCategoryCount + " sub, " + dateCount + " dates]";
        }

        return name + levelIndicator + childInfo;
    }

    private String getDateDisplayName() {
        String date = dateData.getDate();
        int itemCount = dateData.getItems() != null ? dateData.getItems().size() : 0;
        return date + " (" + itemCount + " items)";
    }

    private String getItemDisplayName() {
        return itemDetail.getItemName() + " - " +
                itemDetail.getItemBrand() + " ($" +
                String.format("%.2f", itemDetail.getItemPrice()) + ")";
    }

    public boolean hasChildren() {
        switch (type) {
            case TYPE_CATEGORY:
                return category.hasChildren();
            case TYPE_DATE:
                return dateData.getItems() != null && !dateData.getItems().isEmpty();
            case TYPE_ITEM:
                return false;
            default:
                return false;
        }
    }

    public boolean isCheckable() {
        return type == TYPE_CATEGORY;
    }

    public boolean canExpand() {
        return type == TYPE_CATEGORY || type == TYPE_DATE;
    }

    // Getters and Setters
    public CategoryItem getCategory() { return category; }
    public DateData getDateData() { return dateData; }
    public ItemDetail getItemDetail() { return itemDetail; }
    public int getType() { return type; }
    public int getLevel() { return level; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) {
        this.isChecked = checked;
        if (type == TYPE_CATEGORY && category != null) {
            category.setChecked(checked);
        }
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        if (type == TYPE_CATEGORY && category != null) {
            category.setExpanded(expanded);
        }
    }

    // Helper method to get the actual level for display (0-based)
    public int getDisplayLevel() {
        return level;
    }

    // Helper method to check if this is a leaf node (no children)
    public boolean isLeaf() {
        return !hasChildren();
    }

    // Helper method to get item count for categories and dates
    public int getItemCount() {
        switch (type) {
            case TYPE_CATEGORY:
                return getTotalItemCount(category);
            case TYPE_DATE:
                return dateData.getItems() != null ? dateData.getItems().size() : 0;
            case TYPE_ITEM:
                return 1;
            default:
                return 0;
        }
    }

    private int getTotalItemCount(CategoryItem category) {
        if (category == null) return 0;

        int count = 0;

        // Count items in dates
        if (category.getDates() != null) {
            for (DateData date : category.getDates()) {
                count += date.getItems() != null ? date.getItems().size() : 0;
            }
        }

        // Recursively count items in subcategories
        if (category.getSubCategories() != null) {
            for (CategoryItem subCategory : category.getSubCategories()) {
                count += getTotalItemCount(subCategory);
            }
        }

        return count;
    }
}