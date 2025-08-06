package com.loyaltyportal.entity;

import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "company")
@AdminPresentationClass(friendlyName = "Company")
public class Company {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(columnDefinition = "uuid")
    @AdminPresentation(friendlyName = "ID", visibility = AdminPresentation.VisibilityEnum.HIDDEN_ALL)
    private UUID id;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    @Column(name = "name", nullable = false)
    @AdminPresentation(friendlyName = "Company Name", order = 1, prominent = true)
    private String name;

    @Column(name = "loyalty_account_id", unique = true, length = 100)
    @AdminPresentation(friendlyName = "Loyalty Account ID", order = 2)
    private String loyaltyAccountId;

    @Column(name = "tier_level", length = 50)
    @AdminPresentation(friendlyName = "Tier Level", order = 3)
    private String tierLevel;

    @Column(name = "is_active")
    @AdminPresentation(friendlyName = "Active", order = 4)
    private Boolean isActive = true;

    @Column(name = "created_at")
    @AdminPresentation(friendlyName = "Created At", order = 5)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @AdminPresentation(friendlyName = "Updated At", order = 6)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AccountManager> accountManagers;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RedemptionOrder> orders;

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
    public Company() {}

    public Company(String name, String loyaltyAccountId, String tierLevel) {
        this.name = name;
        this.loyaltyAccountId = loyaltyAccountId;
        this.tierLevel = tierLevel;
        this.isActive = true;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoyaltyAccountId() {
        return loyaltyAccountId;
    }

    public void setLoyaltyAccountId(String loyaltyAccountId) {
        this.loyaltyAccountId = loyaltyAccountId;
    }

    public String getTierLevel() {
        return tierLevel;
    }

    public void setTierLevel(String tierLevel) {
        this.tierLevel = tierLevel;
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

    public List<AccountManager> getAccountManagers() {
        return accountManagers;
    }

    public void setAccountManagers(List<AccountManager> accountManagers) {
        this.accountManagers = accountManagers;
    }

    public List<RedemptionOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<RedemptionOrder> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id != null && id.equals(company.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", loyaltyAccountId='" + loyaltyAccountId + '\'' +
                ", tierLevel='" + tierLevel + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}