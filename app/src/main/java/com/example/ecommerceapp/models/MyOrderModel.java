package com.example.ecommerceapp.models;

public class MyOrderModel {
    String orderId;
    String productName;
    String productImage;
    double totalAmount;
    String paymentStatus;
    com.google.firebase.Timestamp orderDate;


    String deliveryAddress;


    String documentId;


    public MyOrderModel() { }

    // Constructor with parameters
    public MyOrderModel(String orderId, String productName, String productImage, double totalAmount, String paymentStatus, com.google.firebase.Timestamp orderDate, String deliveryAddress) {
        this.orderId = orderId;
        this.productName = productName;
        this.productImage = productImage;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
    }

    // --- Getters and Setters ---

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public com.google.firebase.Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(com.google.firebase.Timestamp orderDate) {
        this.orderDate = orderDate;
    }


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}