package com.example.myapplication.adapters;

public class Expense {

    private Integer id;
    private String expensePart;
    private Integer expenseAmount;
    private String expenseDateTime;
    private String expenseDate;
    private Integer yesterdaysBalance;
    private Integer sales;
    private Integer balance;
    private Boolean isChecked = false;

    public Expense() {
        // Required empty constructor for Firebase
    }

    public Expense(Integer id, String expensePart, Integer expenseAmount, String expenseDateTime, String expenseDate, Integer yesterdaysBalance, Integer sales, Integer balance) {
        this.id = id;
        this.expensePart = expensePart;
        this.expenseAmount = expenseAmount;
        this.expenseDateTime = expenseDateTime;
        this.expenseDate = expenseDate;
        this.yesterdaysBalance = yesterdaysBalance;
        this.sales = sales;
        this.balance = balance;
    }
    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExpensePart() {
        return expensePart;
    }

    public void setExpensePart(String expensePart) {
        this.expensePart = expensePart;
    }

    public Integer getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(Integer expenseAmount) {
        this.expenseAmount = expenseAmount;
    }

    public String getExpenseDateTime() {
        return expenseDateTime;
    }

    public void setExpenseDateTime(String expenseDateTime) {
        this.expenseDateTime = expenseDateTime;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getYesterdaysBalance() {
        return yesterdaysBalance;
    }

    public void setYesterdaysBalance(Integer yesterdaysBalance) {
        this.yesterdaysBalance = yesterdaysBalance;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }
}

