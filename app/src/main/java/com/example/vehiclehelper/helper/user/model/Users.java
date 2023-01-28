package com.example.vehiclehelper.helper.user.model;

import java.io.Serializable;

public class Users implements Serializable {
    private String uid;
    private String pwd;
    private String name;
    // Token notification
    private String token;
    private VehicleInfo vehicleInfo;
    // Mặc định là người dùng
    public TYPE type = TYPE.CUSTOMER;

    public VehicleInfo getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(VehicleInfo vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void putStoreID(String storeID) {
//        if (this.storeIDs == null) this.storeIDs = new ArrayList<>();
//        storeIDs.add(storeID);
//    }

    public enum TYPE implements Serializable {
        STORE, CUSTOMER
    }
}
