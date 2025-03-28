package com.example.myapplication.dtos;

import java.util.ArrayList;
import java.util.List;

public class Month {

    private String monthName;
    private Long monthTotal;
    private List<Day> dayList = new ArrayList<>();

    private Boolean isExpanded;

    public Long getMonthTotal() {
        return monthTotal;
    }

    public void setMonthTotal(Long monthTotal) {
        this.monthTotal = monthTotal;
    }

    public Boolean getExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        isExpanded = expanded;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    @Override
    public String toString() {
        return "Month{" +
                "monthName='" + monthName + '\'' +
                ", dayList=" + dayList +
                '}';
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
    public Month(){

    }
    public Month(String monthName, List<Day> dayList, Boolean isExpanded) {
        this.monthName = monthName;
        this.dayList = dayList;
        this.isExpanded = isExpanded;
    }
}
