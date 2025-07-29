package com.example.artisan;

import android.net.Uri;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentName;
    private String commentText;
    private String commentProfileUrl;
    private String commentId;
    private String commentUserId,productId;
    private com.google.firebase.Timestamp createdAt;

    public Comment(String commentProfileUrl, String commentName,String commentText,String commentId,String commentUserId,String productId){
        this.commentName=commentName;
        this.commentText=commentText;
        this.commentProfileUrl=commentProfileUrl;
        this.commentId=commentId;
        this.commentUserId=commentUserId;
        this.productId=productId;
    }

    public String getProductId() {
        return productId;
    }

    public String getCommentUserId() {
        return commentUserId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getCommentProfileUrl() {
        return commentProfileUrl;
    }

    public String getCommentName() {
        return commentName;
    }

    public String getCommentText() {
        return commentText;
    }
}
