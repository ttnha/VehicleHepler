package com.example.vehiclehelper.helper.fcm.model;

public class NotificationModel {
    public String to;
    public Data data;

    public static class Data {
        public String group;
        public String content;
        public String senderID;
        public String senderName;
        public String receiverID;
        public String receiverName;

        public String cusName;
        public String storeName;

        public String isRating;
    }
}
