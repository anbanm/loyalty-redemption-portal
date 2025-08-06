package com.loyaltyportal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TransactionRequest {
    
    @NotBlank(message = "Account ID is required")
    @JsonProperty("account_id")
    private String accountId;
    
    @NotNull(message = "Points amount is required")
    @Min(value = 1, message = "Points amount must be positive")
    @JsonProperty("points")
    private Integer points;
    
    @NotBlank(message = "Reference is required")
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("metadata")
    private Object metadata;

    // Default constructor for Jackson
    public TransactionRequest() {}

    public TransactionRequest(String accountId, Integer points, String reference) {
        this.accountId = accountId;
        this.points = points;
        this.reference = reference;
    }

    public TransactionRequest(String accountId, Integer points, String reference, String description) {
        this.accountId = accountId;
        this.points = points;
        this.reference = reference;
        this.description = description;
    }

    // Getters and Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "accountId='" + accountId + '\'' +
                ", points=" + points +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}