package com.example.myapplication;

public class ItemDetail {
    private String itemName;
    private String itemBrand;
    private double itemPrice;
    private String purchaseDateTime;
    private String imageUrl;
    private String description;
    private String itemLinks;
    private String itemDoc;
    private String remarks;

    private String capturedImageBase64;


    public ItemDetail() {}

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemBrand() { return itemBrand; }
    public void setItemBrand(String itemBrand) { this.itemBrand = itemBrand; }

    public double getItemPrice() { return itemPrice; }
    public void setItemPrice(double itemPrice) { this.itemPrice = itemPrice; }

    public String getPurchaseDateTime() { return purchaseDateTime; }
    public void setPurchaseDateTime(String purchaseDateTime) { this.purchaseDateTime = purchaseDateTime; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemLinks() { return itemLinks; }
    public void setItemLinks(String itemLinks) { this.itemLinks = itemLinks; }

    public String getItemDoc() { return itemDoc; }
    public void setItemDoc(String itemDoc) { this.itemDoc = itemDoc; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCapturedImageBase64() {
        return capturedImageBase64;
    }

    public void setCapturedImageBase64(String capturedImageBase64) {
        this.capturedImageBase64 = capturedImageBase64;
    }

    public String getFormattedDateTime() {
        if (purchaseDateTime == null) return "N/A";
        try {
            String dateTimeStr = purchaseDateTime.replace(" ", "T");
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateTimeStr);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return purchaseDateTime;
        }
    }
}