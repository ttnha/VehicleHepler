package com.example.vehiclehelper.helper.store.model;

import com.example.vehiclehelper.helper.common.Utils;

public class StoreRating {
    private String id;
    private float rate;
    private String rateContent;
    private String serviceContent;
    private String assessorName;
    private String createDate;

    public StoreRating(String id, float rate, String rateContent, String serviceContent, String assessorName) {
        this.id = id;
        this.rate = rate;
        this.rateContent = rateContent;
        this.serviceContent = serviceContent;
        this.assessorName = assessorName;
        this.createDate = Utils.buildCurrentDate();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getRateContent() {
        return rateContent;
    }

    public void setRateContent(String rateContent) {
        this.rateContent = rateContent;
    }

    public String getServiceContent() {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent) {
        this.serviceContent = serviceContent;
    }

    public String getAssessorName() {
        return assessorName;
    }

    public void setAssessorName(String assessorName) {
        this.assessorName = assessorName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
