package com.loyaltyportal.entity;

public enum TransactionType {
    DEBIT("Debit"),
    CREDIT("Credit"),
    REFUND("Refund");

    private final String displayName;

    TransactionType(String displayName) {
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