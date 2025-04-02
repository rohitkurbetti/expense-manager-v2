package com.example.myapplication;

public class ExpenseRecyclerView {
    private Integer id;
    private String expensePart;
    private Integer expenseAmount;
    private String expenseDateTime;
    private String expenseDate;
    private Integer yesterdaysBalance;
    private Integer sales;
    private Integer balance;

    public ExpenseRecyclerView(int id, String part, int expAmount, String expDate, int sales) {
        this.id=id;
        this.expensePart=part;
        this.expenseAmount=expAmount;
        this.expenseDate=expDate;
        this.sales=sales;

    }

    public ExpenseRecyclerView(Integer id, String expensePart, Integer expenseAmount, String expenseDateTime, String expenseDate, Integer yesterdaysBalance, Integer sales, Integer balance) {
        this.id = id;
        this.expensePart = expensePart;
        this.expenseAmount = expenseAmount;
        this.expenseDateTime = expenseDateTime;
        this.expenseDate = expenseDate;
        this.yesterdaysBalance = yesterdaysBalance;
        this.sales = sales;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "ExpenseRecyclerView{" +
                "id=" + id +
                ", expensePart='" + expensePart + '\'' +
                ", expenseAmount=" + expenseAmount +
                ", expenseDateTime='" + expenseDateTime + '\'' +
                ", expenseDate='" + expenseDate + '\'' +
                ", yesterdaysBalance=" + yesterdaysBalance +
                ", sales=" + sales +
                ", balance=" + balance +
                '}';
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
