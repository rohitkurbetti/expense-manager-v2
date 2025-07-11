package com.example.myapplication.dtos;

import java.util.List;

public class ParentItemYear {

    public String title;
    public List<SubItemMonth> subItems;

    public ParentItemYear(String title, List<SubItemMonth> subItems) {
        this.title = title;
        this.subItems = subItems;
    }

}
