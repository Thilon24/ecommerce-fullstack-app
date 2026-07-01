package com.example.ecommerceapp.models;

public class ChatMessage {

        private String text;
        private String senderId;
        private Object timestamp;
        private boolean isAdmin;

        public ChatMessage() {}

        public ChatMessage(String text, String senderId, Object timestamp, boolean isAdmin) {
            this.text = text;
            this.senderId = senderId;
            this.timestamp = timestamp;
            this.isAdmin = isAdmin;
        }

        // Getters
        public String getText() { return text; }
        public String getSenderId() { return senderId; }
        public Object getTimestamp() { return timestamp; }
        public boolean getIsAdmin() { return isAdmin; }
    }

