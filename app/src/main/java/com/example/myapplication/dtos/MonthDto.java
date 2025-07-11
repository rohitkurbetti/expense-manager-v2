package com.example.myapplication.dtos;

import java.util.List;

public class MonthDto {

    private String month;
    private List<DayDto> dayDtoList;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public List<DayDto> getDayDtoList() {
        return dayDtoList;
    }

    public void setDayDtoList(List<DayDto> dayDtoList) {
        this.dayDtoList = dayDtoList;
    }
}
