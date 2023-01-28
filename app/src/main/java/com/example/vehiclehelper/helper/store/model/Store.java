package com.example.vehiclehelper.helper.store.model;

import com.example.vehiclehelper.helper.common.MyLatLng;

public class Store {
    private String storeID;
    private String storeName;
    private String phone;
    private String addressName;
    private String createDate;
    private MyLatLng latLng;
    private String owner; //uid
    // Trạng thái hoạt động của cửa hàng
    private boolean isActive;
    public transient boolean isUpdate;
    // Lưu vị trí hiện tại của người dùng tới cửa hàng
    public transient double distance;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        if (addressName != null) {
            addressName = addressName.replace("Thành phố", "TP.").replace(", Việt Nam", "");
        }
        this.addressName = addressName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public MyLatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(MyLatLng latLng) {
        this.latLng = latLng;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
