package com.example.myapplication.dtos;


import java.util.List;

// Model for the RecyclerView
public class MainItem {
    private String title;
    private String totalExpense; // List of items for the nested ListView
    private boolean isExpanded;  // For toggling expansion

    public MainItem(String title,String totalExpense) {
        this.title = title;
        this.totalExpense = totalExpense;
        this.isExpanded = false;  // Initially collapsed
    }

    public String getTitle() {
        return title;
    }

    public String getTotalExpense() {
        return totalExpense;
    }


    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

}
