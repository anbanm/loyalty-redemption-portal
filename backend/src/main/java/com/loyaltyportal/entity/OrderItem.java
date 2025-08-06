package com.loyaltyportal.entity;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_item")
@AdminPresentationClass(friendlyName = "Order Item")
public class OrderItem {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    @AdminPresentation(friendlyName = "ID", visibility = AdminPresentation.VisibilityEnum.HIDDEN_ALL)
    private UUID id;

    @NotNull(message = "Order is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @AdminPresentation(friendlyName = "Order", order = 1, prominent = true)
    private RedemptionOrder order;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @AdminPresentation(friendlyName = "Product", order = 2, prominent = true)
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    @AdminPresentation(friendlyName = "Quantity", order = 3, prominent = true)
    private Integer quantity;

    @NotNull(message = "Points per item is required")
    @Min(value = 1, message = "Points per item must be at least 1")
    @Column(name = "points_per_item", nullable = false)
    @AdminPresentation(friendlyName = "Points Per Item", order = 4, prominent = true)
    private Integer pointsPerItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", length = 50)
    @AdminPresentation(friendlyName = "Fulfillment Status", order = 5)
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    @Column(name = "fulfillment_reference")
    @AdminPresentation(friendlyName = "Fulfillment Reference", order = 6)
    private String fulfillmentReference;

    @Column(name = "tracking_number")
    @AdminPresentation(friendlyName = "Tracking Number", order = 7)
    private String trackingNumber;

    @Column(name = "delivered_at")
    @AdminPresentation(friendlyName = "Delivered At", order = 8)
    private LocalDateTime deliveredAt;

    @Column(name = "created_at")
    @AdminPresentation(friendlyName = "Created At", order = 9)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Business methods
    public Integer getTotalPoints() {
        return quantity * pointsPerItem;
    }

    public boolean isPhysicalProduct() {
        return product != null && ProductType.PHYSICAL.equals(product.getProductType());
    }

    public boolean isVirtualProduct() {
        return product != null && ProductType.VIRTUAL.equals(product.getProductType());
    }

    public void markAsFulfilled(String reference) {
        this.fulfillmentStatus = FulfillmentStatus.FULFILLED;
        this.fulfillmentReference = reference;
    }

    public void markAsShipped(String trackingNumber) {
        if (!isPhysicalProduct()) {
            throw new IllegalStateException("Only physical products can be shipped");
        }
        this.fulfillmentStatus = FulfillmentStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
    }

    public void markAsDelivered() {
        if (!FulfillmentStatus.SHIPPED.equals(fulfillmentStatus)) {
            throw new IllegalStateException("Item must be shipped before it can be delivered");
        }
        this.fulfillmentStatus = FulfillmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.fulfillmentStatus = FulfillmentStatus.FAILED;
        this.fulfillmentReference = reason;
    }

    // Constructors
    public OrderItem() {}

    public OrderItem(RedemptionOrder order, Product product, Integer quantity, Integer pointsPerItem) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.pointsPerItem = pointsPerItem;
        this.fulfillmentStatus = FulfillmentStatus.PENDING;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPointsPerItem() {
        return pointsPerItem;
    }

    public void setPointsPerItem(Integer pointsPerItem) {
        this.pointsPerItem = pointsPerItem;
    }

    public FulfillmentStatus getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public void setFulfillmentStatus(FulfillmentStatus fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
    }

    public String getFulfillmentReference() {
        return fulfillmentReference;
    }

    public void setFulfillmentReference(String fulfillmentReference) {
        this.fulfillmentReference = fulfillmentReference;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
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
        OrderItem orderItem = (OrderItem) o;
        return id != null && id.equals(orderItem.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", product=" + (product != null ? product.getSku() : null) +
                ", quantity=" + quantity +
                ", pointsPerItem=" + pointsPerItem +
                ", fulfillmentStatus=" + fulfillmentStatus +
                '}';
    }
}