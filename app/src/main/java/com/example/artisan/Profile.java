package com.example.artisan;

public class Profile {
    private String sellerProfileUrl,sellerName,sellerId;
    private Double rating;
    private Long totalLikes;

    public Profile(String sellerProfileUrl,String sellerName,Double rating,Long totalLikes,String sellerId){
        this.sellerProfileUrl=sellerProfileUrl;
        this.sellerName=sellerName;
        this.rating=rating;
        this.totalLikes=totalLikes;
        this.sellerId=sellerId;
    }
    public String getSellerProfileUrl(){
        return sellerProfileUrl;
    }
    public String getSellerName(){
        return sellerName;
    }
    public Double getRating(){
        return rating;
    }
    public Long getTotalLikes(){
        return totalLikes;
    }
    public String getSellerId() {
        return sellerId;
    }
}
