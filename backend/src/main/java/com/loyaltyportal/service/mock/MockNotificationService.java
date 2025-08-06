package com.loyaltyportal.service.mock;

import com.loyaltyportal.entity.OrderItem;
import com.loyaltyportal.entity.RedemptionOrder;
import com.loyaltyportal.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "loyalty.notification.mock.enabled", havingValue = "true")
public class MockNotificationService extends NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MockNotificationService.class);

    // Mock email storage
    private final List<MockEmailRecord> sentEmails = new ArrayList<>();
    private final Map<String, List<MockEmailRecord>> emailsByRecipient = new HashMap<>();

    public MockNotificationService() {
        super(null); // No real JavaMailSender needed for mock
        logger.info("Mock Notification Service initialized");
    }

    @Override
    public void sendOrderConfirmation(RedemptionOrder order) {
        logger.info("MOCK: Sending order confirmation email for order: {}", order.getOrderNumber());
        
        String recipient = order.getAccountManager().getEmail();
        String subject = "Order Confirmation - " + order.getOrderNumber();
        String body = buildMockOrderConfirmationEmail(order);
        
        recordSentEmail(recipient, subject, body, "ORDER_CONFIRMATION");
        
        // Simulate email sending delay
        simulateEmailDelay(100, 300);
        
        logger.info("MOCK: Order confirmation email sent to: {}", recipient);
    }

    @Override
    public void sendPhysicalFulfillmentNotification(RedemptionOrder order, List<OrderItem> physicalItems) {
        logger.info("MOCK: Sending physical fulfillment notification for order: {} with {} items", 
                order.getOrderNumber(), physicalItems.size());
        
        // This would typically go to fulfillment team
        String recipient = "fulfillment-team@loyalty-portal.com";
        String subject = "Physical Fulfillment Required - " + order.getOrderNumber();
        String body = buildMockPhysicalFulfillmentEmail(order, physicalItems);
        
        recordSentEmail(recipient, subject, body, "PHYSICAL_FULFILLMENT");
        
        logger.info("MOCK: Physical fulfillment notification logged for fulfillment team");
    }

    @Override
    public void sendVirtualFulfillmentNotification(OrderItem item, Object response) {
        logger.info("MOCK: Sending virtual fulfillment notification for item: {}", item.getProduct().getSku());
        
        String recipient = item.getOrder().getAccountManager().getEmail();
        String subject = "Virtual Item Delivered - " + item.getOrder().getOrderNumber();
        String body = buildMockVirtualFulfillmentEmail(item);
        
        recordSentEmail(recipient, subject, body, "VIRTUAL_FULFILLMENT");
        
        simulateEmailDelay(50, 200);
        
        logger.info("MOCK: Virtual fulfillment email sent to: {}", recipient);
    }

    @Override
    public void sendFulfillmentFailureNotification(OrderItem item, String errorMessage) {
        logger.warn("MOCK: Sending fulfillment failure notification for item: {} error: {}", 
                item.getProduct().getSku(), errorMessage);
        
        // Send to both customer and operations team
        String customerRecipient = item.getOrder().getAccountManager().getEmail();
        String opsRecipient = "operations@loyalty-portal.com";
        
        String subject = "Fulfillment Issue - " + item.getOrder().getOrderNumber();
        String customerBody = buildMockFailureEmailForCustomer(item, errorMessage);
        String opsBody = buildMockFailureEmailForOps(item, errorMessage);
        
        recordSentEmail(customerRecipient, subject, customerBody, "FULFILLMENT_FAILURE_CUSTOMER");
        recordSentEmail(opsRecipient, subject, opsBody, "FULFILLMENT_FAILURE_OPS");
        
        logger.warn("MOCK: Fulfillment failure notifications logged");
    }

    @Override
    public void sendShippingNotification(OrderItem item) {
        logger.info("MOCK: Sending shipping notification for item: {} tracking: {}", 
                item.getProduct().getSku(), item.getTrackingNumber());
        
        String recipient = item.getOrder().getAccountManager().getEmail();
        String subject = "Item Shipped - " + item.getOrder().getOrderNumber();
        String body = buildMockShippingEmail(item);
        
        recordSentEmail(recipient, subject, body, "SHIPPING_NOTIFICATION");
        
        simulateEmailDelay(80, 250);
        
        logger.info("MOCK: Shipping notification email sent to: {}", recipient);
    }

    @Override
    public void sendDeliveryNotification(OrderItem item) {
        logger.info("MOCK: Sending delivery notification for item: {}", item.getProduct().getSku());
        
        String recipient = item.getOrder().getAccountManager().getEmail();
        String subject = "Item Delivered - " + item.getOrder().getOrderNumber();
        String body = buildMockDeliveryEmail(item);
        
        recordSentEmail(recipient, subject, body, "DELIVERY_NOTIFICATION");
        
        simulateEmailDelay(50, 150);
        
        logger.info("MOCK: Delivery notification email sent to: {}", recipient);
    }

    @Override
    public void sendOrderCompletionNotification(RedemptionOrder order) {
        logger.info("MOCK: Sending order completion notification for: {}", order.getOrderNumber());
        
        String recipient = order.getAccountManager().getEmail();
        String subject = "Order Complete - " + order.getOrderNumber();
        String body = buildMockOrderCompletionEmail(order);
        
        recordSentEmail(recipient, subject, body, "ORDER_COMPLETION");
        
        simulateEmailDelay(100, 200);
        
        logger.info("MOCK: Order completion email sent to: {}", recipient);
    }

    // Mock-specific methods for testing
    public List<MockEmailRecord> getAllSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    public List<MockEmailRecord> getEmailsByRecipient(String recipient) {
        return emailsByRecipient.getOrDefault(recipient, new ArrayList<>());
    }

    public List<MockEmailRecord> getEmailsByType(String type) {
        return sentEmails.stream()
                .filter(email -> type.equals(email.getType()))
                .collect(Collectors.toList());
    }

    public long getEmailCount() {
        return sentEmails.size();
    }

    public long getEmailCountByType(String type) {
        return sentEmails.stream()
                .filter(email -> type.equals(email.getType()))
                .count();
    }

    public void clearEmailHistory() {
        logger.info("MOCK: Clearing email history");
        sentEmails.clear();
        emailsByRecipient.clear();
    }

    public NotificationStatistics getStatistics() {
        Map<String, Long> countsByType = sentEmails.stream()
                .collect(Collectors.groupingBy(MockEmailRecord::getType, Collectors.counting()));
        
        return new NotificationStatistics(sentEmails.size(), countsByType);
    }

    // Private helper methods
    private void recordSentEmail(String recipient, String subject, String body, String type) {
        MockEmailRecord email = new MockEmailRecord(recipient, subject, body, type, LocalDateTime.now());
        sentEmails.add(email);
        
        emailsByRecipient.computeIfAbsent(recipient, k -> new ArrayList<>()).add(email);
        
        logger.debug("MOCK: Recorded email - Type: {}, Recipient: {}, Subject: {}", type, recipient, subject);
    }

    private void simulateEmailDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int)(Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Mock email builders (simplified versions)
    private String buildMockOrderConfirmationEmail(RedemptionOrder order) {
        return String.format("MOCK Order Confirmation\nOrder: %s\nCompany: %s\nTotal Points: %d\nItems: %d",
                order.getOrderNumber(), order.getCompany().getName(), 
                order.getTotalPoints(), order.getItems().size());
    }

    private String buildMockPhysicalFulfillmentEmail(RedemptionOrder order, List<OrderItem> items) {
        return String.format("MOCK Physical Fulfillment Required\nOrder: %s\nItems: %d\nShipping Address: %s",
                order.getOrderNumber(), items.size(), 
                order.getShippingAddress() != null ? order.getShippingAddress() : "Not provided");
    }

    private String buildMockVirtualFulfillmentEmail(OrderItem item) {
        return String.format("MOCK Virtual Item Delivered\nProduct: %s\nOrder: %s\nDelivered At: %s",
                item.getProduct().getName(), item.getOrder().getOrderNumber(), 
                item.getDeliveredAt());
    }

    private String buildMockFailureEmailForCustomer(OrderItem item, String error) {
        return String.format("MOCK Fulfillment Issue (Customer)\nProduct: %s\nOrder: %s\nWe're working to resolve: %s",
                item.getProduct().getName(), item.getOrder().getOrderNumber(), error);
    }

    private String buildMockFailureEmailForOps(OrderItem item, String error) {
        return String.format("MOCK Fulfillment Issue (Ops)\nProduct: %s\nOrder: %s\nError: %s\nAction Required!",
                item.getProduct().getName(), item.getOrder().getOrderNumber(), error);
    }

    private String buildMockShippingEmail(OrderItem item) {
        return String.format("MOCK Item Shipped\nProduct: %s\nOrder: %s\nTracking: %s",
                item.getProduct().getName(), item.getOrder().getOrderNumber(), 
                item.getTrackingNumber());
    }

    private String buildMockDeliveryEmail(OrderItem item) {
        return String.format("MOCK Item Delivered\nProduct: %s\nOrder: %s\nDelivered At: %s",
                item.getProduct().getName(), item.getOrder().getOrderNumber(), 
                item.getDeliveredAt());
    }

    private String buildMockOrderCompletionEmail(RedemptionOrder order) {
        return String.format("MOCK Order Complete\nOrder: %s\nCompleted At: %s\nThank you for your business!",
                order.getOrderNumber(), order.getCompletedAt());
    }

    // Data classes
    public static class MockEmailRecord {
        private final String recipient;
        private final String subject;
        private final String body;
        private final String type;
        private final LocalDateTime sentAt;

        public MockEmailRecord(String recipient, String subject, String body, String type, LocalDateTime sentAt) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
            this.type = type;
            this.sentAt = sentAt;
        }

        // Getters
        public String getRecipient() { return recipient; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public String getType() { return type; }
        public LocalDateTime getSentAt() { return sentAt; }
    }

    public static class NotificationStatistics {
        private final long totalEmails;
        private final Map<String, Long> emailsByType;

        public NotificationStatistics(long totalEmails, Map<String, Long> emailsByType) {
            this.totalEmails = totalEmails;
            this.emailsByType = emailsByType;
        }

        public long getTotalEmails() { return totalEmails; }
        public Map<String, Long> getEmailsByType() { return emailsByType; }
    }
}