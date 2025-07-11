package com.example.myapplication.adapterholders;

import java.io.Serializable;
import java.util.Objects;

public class CustomItem implements Serializable {

    private String name;
    private boolean checked;
    private float sliderValue;
    private int amount;

    public CustomItem() {

    }

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

    @Override
    public String toString() {
        return "CustomItem{" +
                "name='" + name + '\'' +
                ", checked=" + checked +
                ", sliderValue=" + sliderValue +
                ", amount=" + amount +
                '}';
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomItem)) return false;
        CustomItem item = (CustomItem) o;
        return Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
