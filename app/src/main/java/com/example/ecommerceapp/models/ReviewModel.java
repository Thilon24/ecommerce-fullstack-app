package com.example.ecommerceapp.models;

import com.google.firebase.Timestamp;

public class ReviewModel {
    String userName;
    String review;
    float rating;
    Timestamp timestamp;
    String reviewImg;


    public ReviewModel() {
    }

    public ReviewModel(String userName, String review, float rating, Timestamp timestamp, String reviewImg) {
        this.userName = userName;
        this.review = review;
        this.rating = rating;
        this.timestamp = timestamp;
        this.reviewImg = reviewImg;
    }


    public String getUserName() {
        return userName;
    }

    public String getReview() {
        return review;
    }

    public float getRating() {
        return rating;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getReviewImg() {
        return reviewImg;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setReviewImg(String reviewImg) { // අලුතින් එක් කළ Setter එක
        this.reviewImg = reviewImg;
    }
}