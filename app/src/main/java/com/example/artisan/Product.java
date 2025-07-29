package com.example.artisan;

import java.sql.Timestamp;
import java.util.List;

public class Product {
    private String imageurl;
    private String name;
    private Long likes;
    private Double price;
    private String productID;
    private com.google.firebase.Timestamp createdAt;
    private List<String> productImageUrls;
    private String aspectRatio;
    private String sellerName;
    private String sellerProfileUrl;
    private Double sellerRating;
    private Long comments;
    private String sellerId;
    private Long orderCount;
    private Double revenue;

    public Product(){}

    public Product(String imageurl,String name,Double price,Long likes,String productID){
        this.imageurl=imageurl;
        this.name=name;
        this.price=price;
        this.likes=likes;
        this.productID=productID;
    }
    public Product(String productID,String sellerProfileUrl,String sellerName,Double sellerRating,List<String> productImageUrls,String name,Long likes,Long comments,Double price,String aspectRatio,String sellerId){
        this.productID=productID;
        this.sellerProfileUrl=sellerProfileUrl;
        this.sellerName=sellerName;
        this.sellerRating=sellerRating;
        this.productImageUrls=productImageUrls;
        this.name=name;
        this.likes=likes;
        this.comments=comments;
        this.price=price;
        this.aspectRatio=aspectRatio;
        this.sellerId=sellerId;
    }
    public Product (String productID,String imageurl,String name,Long likes,Long orderCount,Double revenue){
        this.productID=productID;
        this.imageurl=imageurl;
        this.name=name;
        this.likes=likes;
        this.orderCount=orderCount;
        this.revenue=revenue;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getSellerProfileUrl(){
        return sellerProfileUrl;
    }
    public String getSellerName(){
        return sellerName;
    }
    public Double getSellerRating() {
        return sellerRating;
    }
    public List<String> getProductImageUrls() {
        return productImageUrls;
    }
    public Long getComments() {
        return comments;
    }
    public String getAspectRatio() {
        return aspectRatio;
    }
    public String getProductID() {
        return productID;
    }
    public String getProductImageurl(){
        return imageurl;
    }
    public String getProductName(){
        return name;
    }
    public Double getProductPrice(){
        return price;
    }
    public Long getProductLikes(){
        return likes;
    }
    public Long getOrderCount(){
        return orderCount;
    }
    public Double getRevenue() {
        return revenue;
    }
    public com.google.firebase.Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
