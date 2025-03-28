package com.example.myapplication.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Year implements Serializable {

    private String yearName;

    private List<Month> monthList = new ArrayList<>();

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public List<Month> getMonthList() {
        return monthList;
    }

    public void setMonthList(List<Month> monthList) {
        this.monthList = monthList;
    }

    @Override
    public String toString() {
        return "Year{" +
                "yearName='" + yearName + '\'' +
                ", monthList=" + monthList +
                '}';
    }
}
