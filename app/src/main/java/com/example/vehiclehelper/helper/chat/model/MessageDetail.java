package com.example.vehiclehelper.helper.chat.model;

import com.google.firebase.database.Exclude;

import java.util.List;

public class MessageDetail {
    private String id;
    private String userID;
    private String content;
    private String sendDate;
    private String serviceID;

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    @Exclude
    public MessageDetail buildMessage(String content, String senderID) {
        this.setContent(content);
        this.setUserID(senderID);
        this.setId(System.currentTimeMillis() + "");
        return this;
    }

    public MessageDetail() {
    }
}
