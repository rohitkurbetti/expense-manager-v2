package com.example.myapplication.dtos;

import com.example.myapplication.adapterholders.CustomItem;

import java.util.List;

public class DtoJsonEntity {

    private Long invoiceId;
    private String name;
    private Long total;
    private String createddtm;
    private String date;
    private String itemListJsonStr;
    private Long qty;

    public Long getQty() {
        return qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

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

    public String getItemListJsonStr() {
        return itemListJsonStr;
    }

    public void setItemListJsonStr(String itemListJsonStr) {
        this.itemListJsonStr = itemListJsonStr;
    }


    @Override
    public String toString() {
        return "DtoJsonEntity{" +
                "invoiceId=" + invoiceId +
                ", name='" + name + '\'' +
                ", total=" + total +
                ", createddtm='" + createddtm + '\'' +
                ", date='" + date + '\'' +
                ", itemListJsonStr='" + itemListJsonStr + '\'' +
                '}';
    }

    public DtoJsonEntity() {

    }

    public DtoJsonEntity(String name, Long total, Long qty) {
        this.name = name;
        this.total = total;
        this.qty = qty;
    }

    public DtoJsonEntity(Long invoiceId, String name, Long total, String createddtm, String date, String itemListJsonStr) {
        this.invoiceId = invoiceId;
        this.name = name;
        this.total = total;
        this.createddtm = createddtm;
        this.date = date;
        this.itemListJsonStr = itemListJsonStr;
    }
}
