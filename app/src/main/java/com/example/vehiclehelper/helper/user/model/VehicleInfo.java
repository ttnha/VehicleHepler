package com.example.vehiclehelper.helper.user.model;

import androidx.annotation.NonNull;

public class VehicleInfo {
    private String distributor; // Nhà phân phối
    private String name; // Tên xe
    private int ounce; // Phân khối
    private String number; // Biển số xe

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOunce() {
        return ounce;
    }

    public void setOunce(int ounce) {
        this.ounce = ounce;
    }

    @NonNull
    @Override
    public String toString() {
        return "VehicleInfo{" +
                "distributor='" + distributor + '\'' +
                ", name='" + name + '\'' +
                ", ounce=" + ounce +
                ", number='" + number + '\'' +
                '}';
    }
}
