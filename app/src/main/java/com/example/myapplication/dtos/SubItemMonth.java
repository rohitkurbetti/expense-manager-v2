package com.example.myapplication.dtos;

import java.util.List;

public class SubItemMonth {

    public String name;
    public List<SubSubItemDay> subSubItems;

    public SubItemMonth(String name, List<SubSubItemDay> subSubItems) {
        this.name = name;
        this.subSubItems = subSubItems;
    }
}
