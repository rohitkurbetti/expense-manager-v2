package com.example.myapplication.dtos;

public class ItemModel {
    private String itemName;
    private int quantity;

    public ItemModel(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
}
