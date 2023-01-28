package com.example.vehiclehelper.helper.chat.model;

public class MessageRecent {
    private String id;
    private String content;
    private String sendDate;
    private String cusID;
    private String cusName;
    private String storeID;
    private String storeName;
    private String senderID;

    public MessageRecent() {
    }

    public MessageRecent(String id, String content, String sendDate, String cusID, String cusName, String storeID, String storeName) {
        this.id = id;
        this.content = content;
        this.sendDate = sendDate;
        this.cusID = cusID;
        this.storeID = storeID;
        this.cusName = cusName;
        this.storeName = storeName;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getCusID() {
        return cusID;
    }

    public void setCusID(String cusID) {
        this.cusID = cusID;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
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
}
