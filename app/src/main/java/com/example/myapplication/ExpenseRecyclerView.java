package com.example.myapplication;

public class ExpenseRecyclerView {
    private Integer id;
    private String expensePart;
    private String expenseDate;
    private Integer expenseAmount;
    private Integer sales;

    public ExpenseRecyclerView(int id, String part, int expAmount, String expDate, int sales) {
        this.id=id;
        this.expensePart=part;
        this.expenseAmount=expAmount;
        this.expenseDate=expDate;
        this.sales=sales;

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

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getExpenseAmount() {
        return expenseAmount;
    }

    public void setExpenseAmount(Integer expenseAmount) {
        this.expenseAmount = expenseAmount;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }
}
