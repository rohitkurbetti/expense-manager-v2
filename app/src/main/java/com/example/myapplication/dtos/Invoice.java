package com.example.myapplication.dtos;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

public class Invoice {
    private int invoiceId;
    private String itemListJson;
    private long total;
    private String createdDateTime;
    private String createdDate;
    private Map<String, Integer> itemSaleMap;

    public Invoice(int invoiceId, String itemListJson, long total, String createdDateTime, String createdDate, Map<String, Integer> itemSaleMap) {
        this.invoiceId = invoiceId;
        this.itemListJson = itemListJson;
        this.total = total;
        this.createdDateTime = createdDateTime;
        this.createdDate = createdDate;
        this.itemSaleMap = itemSaleMap;
    }

    public Map<String, Integer> getItemSaleMap() {
        return itemSaleMap;
    }

    public void setItemSaleMap(Map<String, Integer> itemSaleMap) {
        this.itemSaleMap = itemSaleMap;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setItemListJson(String itemListJson) {
        this.itemListJson = itemListJson;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public String getItemListJson() {
        return itemListJson;
    }

    public double getTotal() {
        return total;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getFormattedItemList() {
        try {
            JSONArray jsonArray = new JSONArray(itemListJson);
            StringBuilder formattedList = new StringBuilder();
            for (int i = 0; i < jsonArray.length(); i++) {
                formattedList.append("- ").append(jsonArray.getString(i)).append("\n");
            }
            return formattedList.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "Invalid JSON";
        }
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", itemListJson='" + itemListJson + '\'' +
                ", total=" + total +
                ", createdDateTime='" + createdDateTime + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", itemSaleMap=" + itemSaleMap +
                '}';
    }
}
