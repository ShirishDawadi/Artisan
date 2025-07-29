package com.example.artisan;

import com.google.firebase.Timestamp;

public class Ordered {

    private String orderId,imageUrl,title,variationX,status,paymentOption;
    private Double totalPrice;
    private Timestamp timestamp;
    public Ordered(String orderId,String imageUrl,String title,String variationX,Double totalPrice,String status,String paymentOption){
        this.orderId=orderId;
        this.imageUrl=imageUrl;
        this.title=title;
        this.variationX=variationX;
        this.totalPrice=totalPrice;
        this.status=status;
        this.paymentOption=paymentOption;
    }
    public Ordered(){

    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public String getOrderId() {
        return orderId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVariationX() {
        return variationX;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentOption() {
        return paymentOption;
    }
}
