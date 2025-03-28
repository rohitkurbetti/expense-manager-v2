package com.example.myapplication.dtos;

public class Item {

    private String itemName;
    private int itemQty;
    private int itemAmount;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemQty() {
        return itemQty;
    }

    public void setItemQty(int itemQty) {
        this.itemQty = itemQty;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(int itemAmount) {
        this.itemAmount = itemAmount;
    }

    public Item(String itemName, int itemQty, int itemAmount) {
        this.itemName = itemName;
        this.itemQty = itemQty;
        this.itemAmount = itemAmount;
    }
}
