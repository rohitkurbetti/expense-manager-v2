package com.example.myapplication.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Day implements Serializable {

    private String dayName;
    private Long dayTotal;
    private List<DtoJsonEntity> dtoJsonEntityList = new ArrayList<>();

    public Long getDayTotal() {
        return dayTotal;
    }

    public void setDayTotal(Long dayTotal) {
        this.dayTotal = dayTotal;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public List<DtoJsonEntity> getDtoJsonEntityList() {
        return dtoJsonEntityList;
    }

    public void setDtoJsonEntityList(List<DtoJsonEntity> dtoJsonEntityList) {
        this.dtoJsonEntityList = dtoJsonEntityList;
    }

    @Override
    public String toString() {
        return "Day{" +
                "dayName='" + dayName + '\'' +
                ", dtoJsonEntityList=" + dtoJsonEntityList +
                '}';
    }
}
