package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.CategoryItem;
import com.example.myapplication.DateData;
import com.example.myapplication.DisplayItem;
import com.example.myapplication.ItemDetail;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends BaseAdapter {

    private Context context;
    private List<DisplayItem> displayList;
    private List<CategoryItem> originalList;
    private OnCategoryClickListener listener;
    private Map<String, Boolean> dateExpansionState;
    private Map<String, Boolean> categoryExpansionState;
    private Map<String, Boolean> checkboxStates = new HashMap<>();

    public interface OnCategoryClickListener {
        void onCategoryClick(DisplayItem item);
        void onCategoryChecked(DisplayItem item, boolean isChecked);
        void onItemDetailClick(ItemDetail itemDetail);
    }

    public CategoryAdapter(Context context, List<CategoryItem> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.originalList = categories != null ? categories : new ArrayList<>();
        this.listener = listener;
        this.displayList = new ArrayList<>();
        this.dateExpansionState = new HashMap<>();
        this.categoryExpansionState = new HashMap<>();
        this.checkboxStates = new HashMap<>();

        initializeAllStates(originalList);
        buildDisplayList();
    }

    private void initializeAllStates(List<CategoryItem> categories) {
        for (CategoryItem category : categories) {
            String categoryKey = category.getId();

            // Set expansion state from category data
            categoryExpansionState.put(categoryKey, category.isExpanded());

            // Auto-check if category is expanded
            if (category.isExpanded()) {
                category.setChecked(true);
                checkboxStates.put(categoryKey, true);
            } else {
                checkboxStates.put(categoryKey, category.isChecked());
            }

            // Recursively initialize subcategories
            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                initializeAllStates(category.getSubCategories());
            }

            // Initialize dates expansion state to false by default
            if (category.getDates() != null) {
                for (DateData dateData : category.getDates()) {
                    String dateKey = category.getId() + "_" + dateData.getDate();
                    dateExpansionState.put(dateKey, false);
                }
            }
        }
    }

    @Override
    public int getCount() {
        return displayList.size();
    }

    @Override
    public DisplayItem getItem(int position) {
        return displayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DisplayItem item = getItem(position);
        holder.bind(item, position);

        return convertView;
    }

    private void buildDisplayList() {
        displayList.clear();
        for (CategoryItem category : originalList) {
            addCategoryToDisplay(category, 0);
        }
    }

    private void addCategoryToDisplay(CategoryItem category, int level) {
        DisplayItem displayItem = new DisplayItem(category, level, DisplayItem.TYPE_CATEGORY);
        displayList.add(displayItem);

        String categoryKey = category.getId();
        Boolean isCategoryExpanded = categoryExpansionState.get(categoryKey);

        if (isCategoryExpanded != null && isCategoryExpanded) {
            // Add subcategories
            for (CategoryItem subCategory : category.getSubCategories()) {
                addCategoryToDisplay(subCategory, level + 1);
            }

            // Add dates if this is a leaf category with dates
            if (category.getSubCategories().isEmpty() && !category.getDates().isEmpty()) {
                for (DateData dateData : category.getDates()) {
                    addDateToDisplay(category, dateData, level + 1);
                }
            }
        }
    }

    private void addDateToDisplay(CategoryItem parentCategory, DateData dateData, int level) {
        String dateKey = parentCategory.getId() + "_" + dateData.getDate();
        DisplayItem dateItem = new DisplayItem(dateData, level, DisplayItem.TYPE_DATE);
        dateItem.setId(dateKey);
        displayList.add(dateItem);

        Boolean isDateExpanded = dateExpansionState.get(dateKey);
        if (isDateExpanded != null && isDateExpanded && dateData.getItems() != null) {
            for (ItemDetail itemDetail : dateData.getItems()) {
                addItemToDisplay(itemDetail, level + 1);
            }
        }
    }

    private void addItemToDisplay(ItemDetail itemDetail, int level) {
        DisplayItem item = new DisplayItem(itemDetail, level, DisplayItem.TYPE_ITEM);
        displayList.add(item);
    }

    public void refreshData() {
        buildDisplayList();
        notifyDataSetChanged();
    }

    public void expandAll() {
        setAllExpansionStates(true);
        setAllCheckboxStates(true);
        refreshData();
    }

    public void collapseAll() {
        setAllExpansionStates(false);
        setAllCheckboxStates(false);
        refreshData();
    }

    private void setAllExpansionStates(boolean expanded) {
        for (String key : categoryExpansionState.keySet()) {
            categoryExpansionState.put(key, expanded);
        }
        for (String key : dateExpansionState.keySet()) {
            dateExpansionState.put(key, expanded);
        }
    }

    private void setAllCheckboxStates(boolean checked) {
        checkboxStates.clear();
        setAllCategoriesCheckedRecursive(originalList, checked);
    }

    private void setAllCategoriesCheckedRecursive(List<CategoryItem> categories, boolean checked) {
        for (CategoryItem category : categories) {
            category.setChecked(checked);
            checkboxStates.put(category.getId(), checked);
            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                setAllCategoriesCheckedRecursive(category.getSubCategories(), checked);
            }
        }
    }

    public List<DisplayItem> getCheckedItems() {
        List<DisplayItem> checkedItems = new ArrayList<>();
        for (DisplayItem item : displayList) {
            if (item.isChecked() && item.isCheckable()) {
                checkedItems.add(item);
            }
        }
        return checkedItems;
    }

    public List<CategoryItem> getAllCheckedCategories() {
        List<CategoryItem> checkedCategories = new ArrayList<>();
        addCheckedCategoriesRecursive(originalList, checkedCategories);
        return checkedCategories;
    }

    private void addCheckedCategoriesRecursive(List<CategoryItem> categories, List<CategoryItem> checkedCategories) {
        for (CategoryItem category : categories) {
            if (category.isChecked()) {
                checkedCategories.add(category);
            }
            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                addCheckedCategoriesRecursive(category.getSubCategories(), checkedCategories);
            }
        }
    }

    public List<String> getAllCheckedCategoriesWithPath() {
        List<String> checkedPaths = new ArrayList<>();
        addCheckedCategoriesWithPathRecursive(originalList, "", checkedPaths);
        return checkedPaths;
    }

    private void addCheckedCategoriesWithPathRecursive(List<CategoryItem> categories, String parentPath, List<String> checkedPaths) {
        for (CategoryItem category : categories) {
            String currentPath = parentPath.isEmpty() ? category.getName() : parentPath + " → " + category.getName();

            if (category.isChecked()) {
                checkedPaths.add(currentPath);
            }

            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                addCheckedCategoriesWithPathRecursive(category.getSubCategories(), currentPath, checkedPaths);
            }
        }
    }

    private class ViewHolder {
        CheckBox checkBox;
        TextView categoryName;
        LinearLayout container;
        View indicator;
        View divider;

        ViewHolder(View view) {
            checkBox = view.findViewById(R.id.checkbox);
            categoryName = view.findViewById(R.id.category_name);
            container = view.findViewById(R.id.container);
            indicator = view.findViewById(R.id.expand_indicator);
            divider = view.findViewById(R.id.divider);
        }

        void bind(DisplayItem item, int position) {
            // Set indentation
            int margin = item.getLevel() * 40;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(margin, 0, 0, 0);
            container.setLayoutParams(params);

            categoryName.setText(item.getDisplayName());

            // Show checkbox only for categories
            if (item.isCheckable()) {
                checkBox.setVisibility(View.VISIBLE);

                Boolean isChecked = checkboxStates.get(item.getCategory().getId());
                if (isChecked == null) {
                    isChecked = item.isChecked();
                    checkboxStates.put(item.getCategory().getId(), isChecked);
                }

                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(isChecked);
                item.setChecked(isChecked);

            } else {
                checkBox.setVisibility(View.GONE);
                checkBox.setChecked(false);
            }

            // Different styling for different types
            if (item.getType() == DisplayItem.TYPE_DATE) {
                categoryName.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                categoryName.setTextSize(14);
            } else if (item.getType() == DisplayItem.TYPE_ITEM) {
                categoryName.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                categoryName.setTextSize(13);
            } else {
                categoryName.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
                categoryName.setTextSize(16);
            }

            // Expand indicator
            boolean showIndicator = item.hasChildren();
            if (showIndicator) {
                indicator.setVisibility(View.VISIBLE);
                boolean isExpanded = false;
                if (item.getType() == DisplayItem.TYPE_CATEGORY) {
                    isExpanded = categoryExpansionState.get(item.getCategory().getId());
                } else if (item.getType() == DisplayItem.TYPE_DATE) {
                    isExpanded = dateExpansionState.get(item.getId());
                }
                indicator.setRotation(isExpanded ? 90 : 0);
            } else {
                indicator.setVisibility(View.INVISIBLE);
            }

            // Show divider for top-level categories
            if (divider != null) {
                divider.setVisibility(item.getLevel() == 0 ? View.VISIBLE : View.GONE);
            }

            // Checkbox listener
            if (item.isCheckable()) {
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    checkboxStates.put(item.getCategory().getId(), isChecked);
                    item.setChecked(isChecked);
                    item.getCategory().setChecked(isChecked);

                    if (listener != null) {
                        listener.onCategoryChecked(item, isChecked);
                    }

                    String key = item.getCategory().getId();

                    if (isChecked) {
                        if (item.hasChildren()) {
                            Boolean currentState = categoryExpansionState.get(key);
                            if (currentState == null || !currentState) {
                                categoryExpansionState.put(key, true);
                                refreshData();
                            }
                        }
                    } else {
                        Boolean currentState = categoryExpansionState.get(key);
                        if (currentState != null && currentState) {
                            categoryExpansionState.put(key, false);
                            refreshData();
                        }
                        uncheckChildrenRecursive(item.getCategory());
                    }
                });
            }

            // Item click listener
            container.setOnClickListener(v -> {
                if (item.getType() == DisplayItem.TYPE_ITEM) {
                    if (listener != null) {
                        listener.onItemDetailClick(item.getItemDetail());
                    }
                } else if (item.getType() == DisplayItem.TYPE_DATE) {
                    String dateKey = item.getId();
                    Boolean currentState = dateExpansionState.get(dateKey);
                    dateExpansionState.put(dateKey, !currentState);
                    refreshData();
                } else if (item.getType() == DisplayItem.TYPE_CATEGORY) {
                    String categoryKey = item.getCategory().getId();
                    Boolean currentState = categoryExpansionState.get(categoryKey);
                    categoryExpansionState.put(categoryKey, !currentState);

                    if (!currentState && !item.isChecked()) {
                        Boolean checkedState = checkboxStates.get(categoryKey);
                        if (checkedState == null || !checkedState) {
                            checkboxStates.put(categoryKey, true);
                            item.setChecked(true);
                            item.getCategory().setChecked(true);
                            if (listener != null) {
                                listener.onCategoryChecked(item, true);
                            }
                        }
                    }

                    refreshData();
                }

                if (listener != null) {
                    listener.onCategoryClick(item);
                }
            });
        }

        private void uncheckChildrenRecursive(CategoryItem category) {
            category.setChecked(false);
            checkboxStates.put(category.getId(), false);
            if (category.getSubCategories() != null) {
                for (CategoryItem child : category.getSubCategories()) {
                    uncheckChildrenRecursive(child);
                }
            }
        }
    }
}