package com.example.myapplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DtoJson {

    private String name;
    private Long total;
    private String createddtm;
    private String date;
    private List<CustomItem> itemList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getCreateddtm() {
        return createddtm;
    }

    public void setCreateddtm(String createddtm) {
        this.createddtm = createddtm;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<CustomItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<CustomItem> itemList) {
        this.itemList = itemList;
    }
}
