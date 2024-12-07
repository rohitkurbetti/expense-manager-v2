package com.example.myapplication.adapterholders;

import java.io.Serializable;
import java.util.List;

public class DataModel implements Serializable {
    private String id; // Document ID
    private String title;
    private String subtitle;
    private String timestamp;
    private String profileImageUrl;
    private String date;
    private List<CustomItem> itemList;
    private List<CustomItem> otherItemsList;
    private String json;

    private boolean isSelected = false;

    // Default constructor required for calls to DataSnapshot.getValue(DataModel.class)
    public DataModel() {
    }

    public DataModel(String id, String title, String subtitle, String timestamp, String profileImageUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
        this.profileImageUrl = profileImageUrl;
    }

    public DataModel(String id, String title, String subtitle, String timestamp, String profileImageUrl, String date) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
        this.profileImageUrl = profileImageUrl;
        this.date = date;
    }

    public DataModel(String title, String subtitle, String timestamp, String profileImageUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.timestamp = timestamp;
        this.profileImageUrl = profileImageUrl;
    }

    public List<CustomItem> getOtherItemsList() {
        return otherItemsList;
    }

    public void setOtherItemsList(List<CustomItem> otherItemsList) {
        this.otherItemsList = otherItemsList;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public List<CustomItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<CustomItem> itemList) {
        this.itemList = itemList;
    }
}


