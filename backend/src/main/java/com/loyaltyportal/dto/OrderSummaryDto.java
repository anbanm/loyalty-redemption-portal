package com.loyaltyportal.dto;

import com.loyaltyportal.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSummaryDto {

    private UUID id;
    private String orderNumber;
    private String companyName;
    private String accountManagerName;
    private Integer totalPoints;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer itemCount;

    // Default constructor
    public OrderSummaryDto() {}

    public OrderSummaryDto(UUID id, String orderNumber, String companyName, String accountManagerName,
                          Integer totalPoints, OrderStatus status, LocalDateTime createdAt, 
                          LocalDateTime completedAt, Integer itemCount) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.companyName = companyName;
        this.accountManagerName = accountManagerName;
        this.totalPoints = totalPoints;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.itemCount = itemCount;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAccountManagerName() {
        return accountManagerName;
    }

    public void setAccountManagerName(String accountManagerName) {
        this.accountManagerName = accountManagerName;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public String toString() {
        return "OrderSummaryDto{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", companyName='" + companyName + '\'' +
                ", totalPoints=" + totalPoints +
                ", status=" + status +
                ", itemCount=" + itemCount +
                '}';
    }
}