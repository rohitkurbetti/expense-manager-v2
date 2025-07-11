package com.example.myapplication.dtos;

import com.example.myapplication.adapters.Expense;

public class DayDto {

    private String day;

    private Expense expense;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }
}
