package com.loyaltyportal.entity;

public enum FulfillmentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    FULFILLED("Fulfilled"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    FulfillmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}