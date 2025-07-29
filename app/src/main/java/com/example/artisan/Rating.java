package com.example.artisan;

import com.google.firebase.Timestamp;

import java.util.List;

public class Rating {
    private List<String> ratingImages;
    private String profileUrl,buyerName,ratingComment;
    private double stars;
    private Timestamp ratingTimestamp;

    public Rating(String profileUrl,String buyerName,String ratingComment,double stars,List<String> ratingImages, Timestamp ratingTimestamp){
        this.profileUrl=profileUrl;
        this.buyerName=buyerName;
        this.ratingComment=ratingComment;
        this.stars=stars;
        this.ratingImages=ratingImages;
        this.ratingTimestamp=ratingTimestamp;
    }
    public String getProfileUrl() {
        return profileUrl;
    }

    public List<String> getRatingImages() {
        return ratingImages;
    }

    public double getStars() {
        return stars;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public String getRatingComment() {
        return ratingComment;
    }
    public Timestamp getRatingTimestamp() {
        return ratingTimestamp;
    }
}

