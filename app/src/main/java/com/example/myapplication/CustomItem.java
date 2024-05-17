package com.example.myapplication;

import androidx.annotation.NonNull;

public class CustomItem {

    private String name;
    private boolean checked;
    private float sliderValue;
    private int amount;

    public CustomItem(String name, boolean checked, float sliderValue) {
        this.name = name;
        this.checked = checked;
        this.sliderValue = sliderValue;
    }

    public CustomItem(String name, boolean checked, float sliderValue, int price) {
        this.name = name;
        this.checked = checked;
        this.sliderValue = sliderValue;
        this.amount = price;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return checked;
    }

    public float getSliderValue() {
        return sliderValue;
    }

    public void setSliderValue(int sliderValue) {
        this.sliderValue = sliderValue;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
