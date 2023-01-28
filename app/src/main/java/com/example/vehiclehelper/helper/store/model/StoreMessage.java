package com.example.vehiclehelper.helper.store.model;

import com.google.firebase.database.Exclude;

public class StoreMessage {
    private String storeID;
    private String storeName;
    @Exclude
    private int notificationCount;

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
