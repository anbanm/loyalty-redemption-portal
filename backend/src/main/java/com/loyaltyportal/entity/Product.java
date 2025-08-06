package com.loyaltyportal.entity;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product")
@AdminPresentationClass(friendlyName = "Product")
public class Product {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    @AdminPresentation(friendlyName = "ID", visibility = AdminPresentation.VisibilityEnum.HIDDEN_ALL)
    private UUID id;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU cannot exceed 100 characters")
    @Column(name = "sku", unique = true, nullable = false)
    @AdminPresentation(friendlyName = "SKU", order = 1, prominent = true)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    @Column(name = "name", nullable = false)
    @AdminPresentation(friendlyName = "Product Name", order = 2, prominent = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @AdminPresentation(friendlyName = "Description", order = 3, fieldType = SupportedFieldType.HTML)
    private String description;

    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    @AdminPresentation(friendlyName = "Product Type", order = 4, prominent = true, 
                      fieldType = SupportedFieldType.BROADLEAF_ENUMERATION,
                      broadleafEnumeration = "com.loyaltyportal.entity.ProductType")
    private ProductType productType;

    @NotNull(message = "Points cost is required")
    @Min(value = 1, message = "Points cost must be at least 1")
    @Column(name = "points_cost", nullable = false)
    @AdminPresentation(friendlyName = "Points Cost", order = 5, prominent = true)
    private Integer pointsCost;

    @Column(name = "retail_price", precision = 10, scale = 2)
    @AdminPresentation(friendlyName = "Retail Price", order = 6)
    private BigDecimal retailPrice;

    @Column(name = "image_url", length = 500)
    @AdminPresentation(friendlyName = "Image URL", order = 7)
    private String imageUrl;

    @Column(name = "category", length = 100)
    @AdminPresentation(friendlyName = "Category", order = 8)
    private String category;

    @Column(name = "brand", length = 100)
    @AdminPresentation(friendlyName = "Brand", order = 9)
    private String brand;

    @Column(name = "weight_kg", precision = 8, scale = 3)
    @AdminPresentation(friendlyName = "Weight (kg)", order = 10)
    private BigDecimal weightKg;

    @Column(name = "dimensions")
    @AdminPresentation(friendlyName = "Dimensions", order = 11)
    private String dimensions;

    @Column(name = "is_active")
    @AdminPresentation(friendlyName = "Active", order = 12)
    private Boolean isActive = true;

    @Column(name = "created_at")
    @AdminPresentation(friendlyName = "Created At", order = 13)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @AdminPresentation(friendlyName = "Updated At", order = 14)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventories;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Product() {}

    public Product(String sku, String name, ProductType productType, Integer pointsCost) {
        this.sku = sku;
        this.name = name;
        this.productType = productType;
        this.pointsCost = pointsCost;
        this.isActive = true;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Inventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<Inventory> inventories) {
        this.inventories = inventories;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", productType=" + productType +
                ", pointsCost=" + pointsCost +
                ", isActive=" + isActive +
                '}';
    }
}