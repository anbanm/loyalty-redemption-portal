package com.loyaltyportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class BalanceResponse {
    
    @JsonProperty("account_id")
    private String accountId;
    
    @JsonProperty("balance")
    private Integer balance;
    
    @JsonProperty("available_balance")
    private Integer availableBalance;
    
    @JsonProperty("pending_balance")
    private Integer pendingBalance;
    
    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;
    
    @JsonProperty("tier_level")
    private String tierLevel;

    // Default constructor for Jackson
    public BalanceResponse() {}

    public BalanceResponse(String accountId, Integer balance, Integer availableBalance, 
                          Integer pendingBalance, LocalDateTime lastUpdated, String tierLevel) {
        this.accountId = accountId;
        this.balance = balance;
        this.availableBalance = availableBalance;
        this.pendingBalance = pendingBalance;
        this.lastUpdated = lastUpdated;
        this.tierLevel = tierLevel;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Integer availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Integer getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(Integer pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getTierLevel() {
        return tierLevel;
    }

    public void setTierLevel(String tierLevel) {
        this.tierLevel = tierLevel;
    }

    @Override
    public String toString() {
        return "BalanceResponse{" +
                "accountId='" + accountId + '\'' +
                ", balance=" + balance +
                ", availableBalance=" + availableBalance +
                ", pendingBalance=" + pendingBalance +
                ", lastUpdated=" + lastUpdated +
                ", tierLevel='" + tierLevel + '\'' +
                '}';
    }
}