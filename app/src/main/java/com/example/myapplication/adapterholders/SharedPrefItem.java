package com.example.myapplication.adapterholders;

public class SharedPrefItem {

    private String itemName;
    private Integer itemValue = 0;

    public SharedPrefItem(){

    }
    public SharedPrefItem(String itemName, Integer itemValue) {
        this.itemName = itemName;
        this.itemValue = itemValue;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getItemValue() {
        return itemValue;
    }

    public void setItemValue(Integer itemValue) {
        this.itemValue = itemValue;
    }
}
