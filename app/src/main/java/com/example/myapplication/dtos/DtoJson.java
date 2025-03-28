package com.example.myapplication.dtos;

import com.example.myapplication.adapterholders.CustomItem;

import java.io.Serializable;
import java.util.List;

public class DtoJson implements Serializable {

    private String name;
    private Long total;
    private String createddtm;
    private String date;
    private List<CustomItem> itemList;
    private List<CustomItem> otherItemsList;

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

    public List<CustomItem> getOtherItemsList() {
        return otherItemsList;
    }

    public void setOtherItemsList(List<CustomItem> otherItemsList) {
        this.otherItemsList = otherItemsList;
    }

    public DtoJson() {

    }
    public DtoJson(String name, Long total, String createddtm, String date, List<CustomItem> itemList, List<CustomItem> otherItemsList) {
        this.name = name;
        this.total = total;
        this.createddtm = createddtm;
        this.date = date;
        this.itemList = itemList;
        this.otherItemsList = otherItemsList;
    }

    @Override
    public String toString() {
        return "DtoJson{" +
                "name='" + name + '\'' +
                ", total=" + total +
                ", createddtm='" + createddtm + '\'' +
                ", date='" + date + '\'' +
                ", itemList=" + itemList +
                ", otherItemsList=" + otherItemsList +
                '}';
    }


}
