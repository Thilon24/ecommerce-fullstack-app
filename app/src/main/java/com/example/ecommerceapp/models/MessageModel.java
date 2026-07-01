package com.example.ecommerceapp.models;

public class MessageModel {
    String message;
    String senderId;
    long timestamp;

    public MessageModel() {}

    public MessageModel(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
}