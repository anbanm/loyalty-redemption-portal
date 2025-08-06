package com.loyaltyportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class TransactionResponse {
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("account_id")
    private String accountId;
    
    @JsonProperty("points")
    private Integer points;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("balance_before")
    private Integer balanceBefore;
    
    @JsonProperty("balance_after")
    private Integer balanceAfter;
    
    @JsonProperty("processed_at")
    private LocalDateTime processedAt;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_message")
    private String errorMessage;

    // Default constructor for Jackson
    public TransactionResponse() {}

    public TransactionResponse(String transactionId, String accountId, Integer points, String reference, String status) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.points = points;
        this.reference = reference;
        this.status = status;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(Integer balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", points=" + points +
                ", reference='" + reference + '\'' +
                ", status='" + status + '\'' +
                ", balanceBefore=" + balanceBefore +
                ", balanceAfter=" + balanceAfter +
                ", processedAt=" + processedAt +
                '}';
    }
}