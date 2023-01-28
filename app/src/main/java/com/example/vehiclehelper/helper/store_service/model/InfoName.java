package com.example.vehiclehelper.helper.store_service.model;

public class InfoName {
    private String cusName;
    private String storeName;

    public InfoName(String cusName, String storeName) {
        this.cusName = cusName;
        this.storeName = storeName;
    }

    public InfoName() {
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
