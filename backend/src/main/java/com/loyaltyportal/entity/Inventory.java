package com.loyaltyportal.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @NotNull(message = "Product is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity cannot be negative")
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @NotNull(message = "Reserved quantity is required")
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "max_quantity")
    private Integer maxQuantity;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }

    // Constructors
    public Inventory() {}

    public Inventory(Product product, Integer quantityAvailable) {
        this.product = product;
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = 0;
    }

    // Business methods
    public Integer getTotalQuantity() {
        return quantityAvailable + quantityReserved;
    }

    public boolean isInStock() {
        return quantityAvailable > 0;
    }

    public boolean canReserve(Integer quantity) {
        return quantity > 0 && quantityAvailable >= quantity;
    }

    public void reserveQuantity(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Cannot reserve " + quantity + " items. Available: " + quantityAvailable);
        }
        quantityAvailable -= quantity;
        quantityReserved += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void releaseReserved(Integer quantity) {
        if (quantity > quantityReserved) {
            throw new IllegalStateException("Cannot release " + quantity + " items. Reserved: " + quantityReserved);
        }
        quantityReserved -= quantity;
        quantityAvailable += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void confirmReserved(Integer quantity) {
        if (quantity > quantityReserved) {
            throw new IllegalStateException("Cannot confirm " + quantity + " items. Reserved: " + quantityReserved);
        }
        quantityReserved -= quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void addStock(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive");
        }
        quantityAvailable += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return reorderPoint != null && quantityAvailable <= reorderPoint;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(Integer quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Integer getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(Integer quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return id != null && id.equals(inventory.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", product=" + (product != null ? product.getSku() : null) +
                ", quantityAvailable=" + quantityAvailable +
                ", quantityReserved=" + quantityReserved +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}