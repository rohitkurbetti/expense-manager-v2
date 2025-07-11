package com.example.myapplication.dtos;

import java.util.List;

public class YearDto {

    private String year;

    private List<MonthDto> monthList;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<MonthDto> getMonthList() {
        return monthList;
    }

    public void setMonthList(List<MonthDto> monthList) {
        this.monthList = monthList;
    }
}
