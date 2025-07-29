package com.example.artisan;

import java.sql.Timestamp;

public class Notification {
    private String profileUrl,title,message,orderId;
    private com.google.firebase.Timestamp timestamp;

    public Notification(String profileUrl,String title, String message,String orderId){
        this.message=message;
        this.profileUrl=profileUrl;
        this.title=title;
        this.orderId=orderId;
    }
    public Notification(String profileUrl,String title, String message){
        this.message=message;
        this.profileUrl=profileUrl;
        this.title=title;
    }

    public com.google.firebase.Timestamp getTimeStamp() {
        return timestamp;
    }
    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
