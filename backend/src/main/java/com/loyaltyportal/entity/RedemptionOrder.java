package com.loyaltyportal.entity;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "redemption_order")
@AdminPresentationClass(friendlyName = "Redemption Order")
public class RedemptionOrder {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    @AdminPresentation(friendlyName = "ID", visibility = AdminPresentation.VisibilityEnum.HIDDEN_ALL)
    private UUID id;

    @Size(max = 50, message = "Order number cannot exceed 50 characters")
    @Column(name = "order_number", unique = true, length = 50)
    @AdminPresentation(friendlyName = "Order Number", order = 1, prominent = true)
    private String orderNumber;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @AdminPresentation(friendlyName = "Company", order = 2, prominent = true)
    private Company company;

    @NotNull(message = "Account manager is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_manager_id", nullable = false)
    @AdminPresentation(friendlyName = "Account Manager", order = 3, prominent = true)
    private AccountManager accountManager;

    @NotNull(message = "Total points is required")
    @Min(value = 1, message = "Total points must be at least 1")
    @Column(name = "total_points", nullable = false)
    @AdminPresentation(friendlyName = "Total Points", order = 4, prominent = true)
    private Integer totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @AdminPresentation(friendlyName = "Status", order = 5, prominent = true)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    @AdminPresentation(friendlyName = "Shipping Address", order = 6)
    private String shippingAddress;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    @AdminPresentation(friendlyName = "Special Instructions", order = 7)
    private String specialInstructions;

    @Column(name = "created_at")
    @AdminPresentation(friendlyName = "Created At", order = 8)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    @AdminPresentation(friendlyName = "Completed At", order = 9)
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    @AdminPresentation(friendlyName = "Cancelled At", order = 10)
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    @AdminPresentation(friendlyName = "Cancellation Reason", order = 11)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoyaltyTransaction> transactions;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
    }

    private String generateOrderNumber() {
        return "LRP-" + System.currentTimeMillis();
    }

    // Business methods
    public boolean isPending() {
        return OrderStatus.PENDING.equals(status);
    }

    public boolean isProcessing() {
        return OrderStatus.PROCESSING.equals(status);
    }

    public boolean isCompleted() {
        return OrderStatus.COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return OrderStatus.CANCELLED.equals(status);
    }

    public void markAsProcessing() {
        if (!isPending()) {
            throw new IllegalStateException("Order can only be marked as processing from pending status");
        }
        this.status = OrderStatus.PROCESSING;
    }

    public void markAsCompleted() {
        if (!isProcessing()) {
            throw new IllegalStateException("Order can only be completed from processing status");
        }
        this.status = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsCancelled(String reason) {
        if (isCompleted()) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean hasPhysicalItems() {
        return items != null && items.stream()
                .anyMatch(item -> ProductType.PHYSICAL.equals(item.getProduct().getProductType()));
    }

    public boolean hasVirtualItems() {
        return items != null && items.stream()
                .anyMatch(item -> ProductType.VIRTUAL.equals(item.getProduct().getProductType()));
    }

    // Constructors
    public RedemptionOrder() {}

    public RedemptionOrder(Company company, AccountManager accountManager, Integer totalPoints) {
        this.company = company;
        this.accountManager = accountManager;
        this.totalPoints = totalPoints;
        this.status = OrderStatus.PENDING;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
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

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
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

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public List<LoyaltyTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<LoyaltyTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedemptionOrder that = (RedemptionOrder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RedemptionOrder{" +
                "id=" + id +
                ", orderNumber='" + orderNumber + '\'' +
                ", company=" + (company != null ? company.getName() : null) +
                ", totalPoints=" + totalPoints +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}