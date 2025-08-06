package com.loyaltyportal.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotNull(message = "Account manager ID is required")
    private UUID accountManagerId;

    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;

    @Size(max = 1000, message = "Shipping address cannot exceed 1000 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
    private String specialInstructions;

    // Default constructor
    public CreateOrderRequest() {}

    public CreateOrderRequest(UUID companyId, UUID accountManagerId, List<OrderItemRequest> items) {
        this.companyId = companyId;
        this.accountManagerId = accountManagerId;
        this.items = items;
    }

    // Getters and Setters
    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getAccountManagerId() {
        return accountManagerId;
    }

    public void setAccountManagerId(UUID accountManagerId) {
        this.accountManagerId = accountManagerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
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

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "companyId=" + companyId +
                ", accountManagerId=" + accountManagerId +
                ", items=" + items.size() + " items" +
                '}';
    }

    // Nested class for order items
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        @javax.validation.constraints.Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        // Default constructor
        public OrderItemRequest() {}

        public OrderItemRequest(UUID productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        // Getters and Setters
        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "OrderItemRequest{" +
                    "productId=" + productId +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}