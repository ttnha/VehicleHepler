package com.example.vehiclehelper.helper.store_service.model;

import com.google.firebase.database.Exclude;

import java.util.List;

public class RequestService {
    private String id; // cusID_StoreID_Timestamps
    private String status; //DONE, PROCESSING, CANCEL
    private String createDate;
    private String content;
    private String phoneNumber;
    private List<String> serviceIDs;
    private double lat;
    private double lng;

    @Exclude
    public transient InfoName infoName;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public RequestService(String id, String createDate, String content, List<String> serviceIDs) {
        this.id = id;
        this.status = RSStatus.PROCESSING.name();
        this.createDate = createDate;
        this.content = content;
        this.serviceIDs = serviceIDs;
    }

    public RequestService() {
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getServiceIDs() {
        return serviceIDs;
    }

    public void setServiceIDs(List<String> serviceIDs) {
        this.serviceIDs = serviceIDs;
    }

    public enum RSStatus {
        DONE, PROCESSING, CANCEL
    }
}
