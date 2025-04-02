package com.example.myapplication.dtos;

import java.io.Serializable;
import java.util.Map;

public class ExpenseParticularsDto implements Serializable {

    private String expenseParticulars;
    private Map<String, Integer> expensePriceMap;

    public String getExpenseParticulars() {
        return expenseParticulars;
    }

    public void setExpenseParticulars(String expenseParticulars) {
        this.expenseParticulars = expenseParticulars;
    }

    public Map<String, Integer> getExpensePriceMap() {
        return expensePriceMap;
    }

    public void setExpensePriceMap(Map<String, Integer> expensePriceMap) {
        this.expensePriceMap = expensePriceMap;
    }

    @Override
    public String toString() {
        return "ExpenseParticularsDto{" +
                "expenseParticulars='" + expenseParticulars + '\'' +
                ", expensePriceMap=" + expensePriceMap +
                '}';
    }
}
