package com.loyaltyportal.service.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "loyalty.fulfillment.virtual.mock.enabled", havingValue = "true")
public class MockVirtualFulfillmentService {

    private static final Logger logger = LoggerFactory.getLogger(MockVirtualFulfillmentService.class);

    // Mock fulfillment tracking
    private final Map<String, VirtualFulfillmentRecord> fulfillmentRecords = new HashMap<>();

    /**
     * Mock virtual fulfillment processing
     */
    public VirtualFulfillmentResponse fulfillVirtualProduct(VirtualFulfillmentRequest request) {
        logger.info("MOCK: Processing virtual fulfillment for product: {} quantity: {} customer: {}", 
                request.getProductSku(), request.getQuantity(), request.getCustomerEmail());
        
        // Simulate processing time
        try {
            Thread.sleep(300 + (int)(Math.random() * 500)); // 300-800ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate occasional failures (3% failure rate)
        if (Math.random() < 0.03) {
            logger.warn("MOCK: Simulated virtual fulfillment failure for product: {}", request.getProductSku());
            return new VirtualFulfillmentResponse(
                null,
                "FAILED",
                "Mock fulfillment system temporarily unavailable"
            );
        }
        
        // Simulate different fulfillment scenarios based on product type
        String fulfillmentId = generateFulfillmentId();
        String status = "SUCCESS";
        String message = generateFulfillmentMessage(request);
        
        // Store fulfillment record
        VirtualFulfillmentRecord record = new VirtualFulfillmentRecord(
            fulfillmentId,
            request.getReferenceId(),
            request.getProductSku(),
            request.getQuantity(),
            request.getCustomerEmail(),
            request.getCustomerName(),
            request.getCompanyName(),
            LocalDateTime.now(),
            status
        );
        fulfillmentRecords.put(fulfillmentId, record);
        
        logger.info("MOCK: Virtual fulfillment successful for product: {} fulfillmentId: {}", 
                request.getProductSku(), fulfillmentId);
        
        return new VirtualFulfillmentResponse(fulfillmentId, status, message);
    }

    /**
     * Get fulfillment status by ID
     */
    public VirtualFulfillmentRecord getFulfillmentStatus(String fulfillmentId) {
        return fulfillmentRecords.get(fulfillmentId);
    }

    /**
     * Get all fulfillment records for a customer
     */
    public Map<String, VirtualFulfillmentRecord> getFulfillmentsByCustomer(String customerEmail) {
        Map<String, VirtualFulfillmentRecord> customerFulfillments = new HashMap<>();
        fulfillmentRecords.entrySet().stream()
                .filter(entry -> entry.getValue().getCustomerEmail().equals(customerEmail))
                .forEach(entry -> customerFulfillments.put(entry.getKey(), entry.getValue()));
        return customerFulfillments;
    }

    /**
     * Reset mock data
     */
    public void resetMockData() {
        logger.info("MOCK: Resetting virtual fulfillment data");
        fulfillmentRecords.clear();
    }

    /**
     * Get fulfillment statistics
     */
    public FulfillmentStatistics getStatistics() {
        long total = fulfillmentRecords.size();
        long successful = fulfillmentRecords.values().stream()
                .mapToInt(record -> "SUCCESS".equals(record.getStatus()) ? 1 : 0)
                .sum();
        long failed = total - successful;
        
        return new FulfillmentStatistics(total, successful, failed);
    }

    // Private helper methods
    private String generateFulfillmentId() {
        return "VF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateFulfillmentMessage(VirtualFulfillmentRequest request) {
        String productType = detectProductType(request.getProductSku());
        
        return switch (productType) {
            case "SOFTWARE" -> "License key and download instructions sent to " + request.getCustomerEmail();
            case "DIGITAL" -> "Digital content delivered to customer portal for " + request.getCustomerName();
            case "COURSE" -> "Course access granted, welcome email sent to " + request.getCustomerEmail();
            case "SUBSCRIPTION" -> "Subscription activated for " + request.getQuantity() + " months";
            default -> "Virtual product delivered successfully to " + request.getCustomerEmail();
        };
    }

    private String detectProductType(String sku) {
        if (sku.contains("SOFTWARE") || sku.contains("LICENSE")) return "SOFTWARE";
        if (sku.contains("COURSE") || sku.contains("TRAINING")) return "COURSE";
        if (sku.contains("SUBSCRIPTION") || sku.contains("SERVICE")) return "SUBSCRIPTION";
        if (sku.contains("DIGITAL") || sku.contains("EBOOK")) return "DIGITAL";
        return "GENERIC";
    }

    // DTOs and Data Classes
    public static class VirtualFulfillmentRequest {
        private String referenceId;
        private String productSku;
        private Integer quantity;
        private String customerEmail;
        private String customerName;
        private String companyName;

        public VirtualFulfillmentRequest() {}

        public VirtualFulfillmentRequest(String referenceId, String productSku, Integer quantity,
                                       String customerEmail, String customerName, String companyName) {
            this.referenceId = referenceId;
            this.productSku = productSku;
            this.quantity = quantity;
            this.customerEmail = customerEmail;
            this.customerName = customerName;
            this.companyName = companyName;
        }

        // Getters and Setters
        public String getReferenceId() { return referenceId; }
        public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
    }

    public static class VirtualFulfillmentResponse {
        private String fulfillmentId;
        private String status;
        private String message;

        public VirtualFulfillmentResponse() {}

        public VirtualFulfillmentResponse(String fulfillmentId, String status, String message) {
            this.fulfillmentId = fulfillmentId;
            this.status = status;
            this.message = message;
        }

        // Getters and Setters
        public String getFulfillmentId() { return fulfillmentId; }
        public void setFulfillmentId(String fulfillmentId) { this.fulfillmentId = fulfillmentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class VirtualFulfillmentRecord {
        private String fulfillmentId;
        private String referenceId;
        private String productSku;
        private Integer quantity;
        private String customerEmail;
        private String customerName;
        private String companyName;
        private LocalDateTime fulfilledAt;
        private String status;

        public VirtualFulfillmentRecord(String fulfillmentId, String referenceId, String productSku,
                                      Integer quantity, String customerEmail, String customerName,
                                      String companyName, LocalDateTime fulfilledAt, String status) {
            this.fulfillmentId = fulfillmentId;
            this.referenceId = referenceId;
            this.productSku = productSku;
            this.quantity = quantity;
            this.customerEmail = customerEmail;
            this.customerName = customerName;
            this.companyName = companyName;
            this.fulfilledAt = fulfilledAt;
            this.status = status;
        }

        // Getters
        public String getFulfillmentId() { return fulfillmentId; }
        public String getReferenceId() { return referenceId; }
        public String getProductSku() { return productSku; }
        public Integer getQuantity() { return quantity; }
        public String getCustomerEmail() { return customerEmail; }
        public String getCustomerName() { return customerName; }
        public String getCompanyName() { return companyName; }
        public LocalDateTime getFulfilledAt() { return fulfilledAt; }
        public String getStatus() { return status; }
    }

    public static class FulfillmentStatistics {
        private final long totalFulfillments;
        private final long successfulFulfillments;
        private final long failedFulfillments;

        public FulfillmentStatistics(long totalFulfillments, long successfulFulfillments, long failedFulfillments) {
            this.totalFulfillments = totalFulfillments;
            this.successfulFulfillments = successfulFulfillments;
            this.failedFulfillments = failedFulfillments;
        }

        public long getTotalFulfillments() { return totalFulfillments; }
        public long getSuccessfulFulfillments() { return successfulFulfillments; }
        public long getFailedFulfillments() { return failedFulfillments; }
        public double getSuccessRate() {
            return totalFulfillments > 0 ? (double) successfulFulfillments / totalFulfillments * 100 : 0;
        }
    }
}