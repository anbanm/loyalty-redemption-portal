package com.loyaltyportal.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loyalty_transaction")
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotNull(message = "Order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RedemptionOrder order;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull(message = "Points amount is required")
    @Column(name = "points_amount", nullable = false)
    private Integer pointsAmount;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Business methods
    public boolean isPending() {
        return TransactionStatus.PENDING.equals(status);
    }

    public boolean isProcessing() {
        return TransactionStatus.PROCESSING.equals(status);
    }

    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(status);
    }

    public boolean isRefunded() {
        return TransactionStatus.REFUNDED.equals(status);
    }

    public void markAsProcessing() {
        if (!isPending()) {
            throw new IllegalStateException("Transaction can only be marked as processing from pending status");
        }
        this.status = TransactionStatus.PROCESSING;
    }

    public void markAsCompleted(String externalTransactionId) {
        if (!isProcessing()) {
            throw new IllegalStateException("Transaction can only be completed from processing status");
        }
        this.status = TransactionStatus.COMPLETED;
        this.externalTransactionId = externalTransactionId;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsRefunded(String externalTransactionId) {
        if (!isCompleted()) {
            throw new IllegalStateException("Only completed transactions can be refunded");
        }
        this.status = TransactionStatus.REFUNDED;
        this.externalTransactionId = externalTransactionId;
        this.processedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return isFailed() && retryCount < 3;
    }

    // Constructors
    public LoyaltyTransaction() {}

    public LoyaltyTransaction(RedemptionOrder order, Company company, Integer pointsAmount, TransactionType transactionType) {
        this.order = order;
        this.company = company;
        this.pointsAmount = pointsAmount;
        this.transactionType = transactionType;
        this.status = TransactionStatus.PENDING;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RedemptionOrder getOrder() {
        return order;
    }

    public void setOrder(RedemptionOrder order) {
        this.order = order;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getPointsAmount() {
        return pointsAmount;
    }

    public void setPointsAmount(Integer pointsAmount) {
        this.pointsAmount = pointsAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoyaltyTransaction that = (LoyaltyTransaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "LoyaltyTransaction{" +
                "id=" + id +
                ", company=" + (company != null ? company.getName() : null) +
                ", pointsAmount=" + pointsAmount +
                ", transactionType=" + transactionType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}