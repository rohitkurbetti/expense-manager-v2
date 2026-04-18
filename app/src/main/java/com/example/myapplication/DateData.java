package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class DateData {
    private String date;
    private List<ItemDetail> items;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<ItemDetail> getItems() { return items; }
    public void setItems(List<ItemDetail> items) { this.items = items; }

    // Helper method to ensure items list exists
    public void ensureItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
    }

    // Helper method to add an item
    public void addItem(ItemDetail item) {
        ensureItems();
        items.add(item);
    }

    // Helper method to get item count
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}