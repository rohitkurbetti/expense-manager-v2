package com.example.myapplication.dtos;

public class NetResult {
    private String month;
    private double sales;
    private double expenses;
    private double profitLoss;
    private String date;

    public NetResult(String month, double sales, double expenses, double profitLoss, String date) {
        this.month = month;
        this.sales = sales;
        this.expenses = expenses;
        this.profitLoss = profitLoss;
        this.date = date;
    }

    // Getters
    public String getMonth() {
        return month;
    }

    public double getSales() {
        return sales;
    }

    public double getExpenses() {
        return expenses;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public String getDate() {
        return date;
    }
}
