package com.example.myapplication;

import java.util.List;

public interface ExpenseCallback {
    void onResult(List<String> expenses);
}
