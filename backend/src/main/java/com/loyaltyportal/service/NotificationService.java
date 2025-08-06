package com.loyaltyportal.service;

import com.loyaltyportal.entity.OrderItem;
import com.loyaltyportal.entity.RedemptionOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    
    @Value("${loyalty.notification.email.from}")
    private String fromEmail;
    
    @Value("${loyalty.notification.email.enabled:true}")
    private boolean emailEnabled;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send order confirmation notification
     */
    public void sendOrderConfirmation(RedemptionOrder order) {
        if (!emailEnabled) {
            logger.debug("Email notifications disabled, skipping order confirmation for: {}", 
                    order.getOrderNumber());
            return;
        }
        
        logger.info("Sending order confirmation for: {}", order.getOrderNumber());
        
        String subject = "Order Confirmation - " + order.getOrderNumber();
        String body = buildOrderConfirmationEmail(order);
        
        sendEmail(order.getAccountManager().getEmail(), subject, body);
    }

    /**
     * Send notification to fulfillment team for physical items
     */
    public void sendPhysicalFulfillmentNotification(RedemptionOrder order, List<OrderItem> physicalItems) {
        logger.info("Sending physical fulfillment notification for order: {} with {} items", 
                order.getOrderNumber(), physicalItems.size());
        
        // In a real implementation, this would send to fulfillment team email or system
        // For now, we'll just log it
        logger.info("FULFILLMENT ALERT: Order {} requires manual fulfillment of {} physical items", 
                order.getOrderNumber(), physicalItems.size());
    }

    /**
     * Send virtual fulfillment success notification
     */
    public void sendVirtualFulfillmentNotification(OrderItem item, Object response) {
        if (!emailEnabled) return;
        
        logger.info("Sending virtual fulfillment notification for item: {}", item.getProduct().getSku());
        
        String subject = "Virtual Item Delivered - " + item.getOrder().getOrderNumber();
        String body = buildVirtualFulfillmentEmail(item);
        
        sendEmail(item.getOrder().getAccountManager().getEmail(), subject, body);
    }

    /**
     * Send fulfillment failure notification
     */
    public void sendFulfillmentFailureNotification(OrderItem item, String errorMessage) {
        logger.warn("Sending fulfillment failure notification for item: {} error: {}", 
                item.getProduct().getSku(), errorMessage);
        
        // In a real implementation, this would alert operations team
        logger.error("FULFILLMENT FAILURE: Item {} in order {} failed: {}", 
                item.getProduct().getSku(), item.getOrder().getOrderNumber(), errorMessage);
    }

    /**
     * Send shipping notification
     */
    public void sendShippingNotification(OrderItem item) {
        if (!emailEnabled) return;
        
        logger.info("Sending shipping notification for item: {} tracking: {}", 
                item.getProduct().getSku(), item.getTrackingNumber());
        
        String subject = "Item Shipped - " + item.getOrder().getOrderNumber();
        String body = buildShippingEmail(item);
        
        sendEmail(item.getOrder().getAccountManager().getEmail(), subject, body);
    }

    /**
     * Send delivery notification
     */
    public void sendDeliveryNotification(OrderItem item) {
        if (!emailEnabled) return;
        
        logger.info("Sending delivery notification for item: {}", item.getProduct().getSku());
        
        String subject = "Item Delivered - " + item.getOrder().getOrderNumber();
        String body = buildDeliveryEmail(item);
        
        sendEmail(item.getOrder().getAccountManager().getEmail(), subject, body);
    }

    /**
     * Send order completion notification
     */
    public void sendOrderCompletionNotification(RedemptionOrder order) {
        if (!emailEnabled) return;
        
        logger.info("Sending order completion notification for: {}", order.getOrderNumber());
        
        String subject = "Order Complete - " + order.getOrderNumber();
        String body = buildOrderCompletionEmail(order);
        
        sendEmail(order.getAccountManager().getEmail(), subject, body);
    }

    // Private helper methods
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            logger.debug("Email sent successfully to: {} subject: {}", to, subject);
            
        } catch (Exception e) {
            logger.error("Failed to send email to: {} subject: {} error: {}", to, subject, e.getMessage());
        }
    }

    private String buildOrderConfirmationEmail(RedemptionOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(order.getAccountManager().getName()).append(",\n\n");
        sb.append("Your loyalty points redemption order has been confirmed:\n\n");
        sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        sb.append("Company: ").append(order.getCompany().getName()).append("\n");
        sb.append("Total Points: ").append(order.getTotalPoints()).append("\n");
        sb.append("Items: ").append(order.getItems().size()).append("\n");
        sb.append("Order Date: ").append(order.getCreatedAt()).append("\n\n");
        
        if (order.getShippingAddress() != null) {
            sb.append("Shipping Address:\n").append(order.getShippingAddress()).append("\n\n");
        }
        
        sb.append("We will notify you when your items are processed and shipped.\n\n");
        sb.append("Thank you for your business!\n\n");
        sb.append("Loyalty Redemption Portal Team");
        
        return sb.toString();
    }

    private String buildVirtualFulfillmentEmail(OrderItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(item.getOrder().getAccountManager().getName()).append(",\n\n");
        sb.append("Your virtual item has been delivered:\n\n");
        sb.append("Product: ").append(item.getProduct().getName()).append("\n");
        sb.append("SKU: ").append(item.getProduct().getSku()).append("\n");
        sb.append("Quantity: ").append(item.getQuantity()).append("\n");
        sb.append("Order: ").append(item.getOrder().getOrderNumber()).append("\n");
        sb.append("Delivered At: ").append(item.getDeliveredAt()).append("\n\n");
        
        if (item.getFulfillmentReference() != null) {
            sb.append("Fulfillment Reference: ").append(item.getFulfillmentReference()).append("\n\n");
        }
        
        sb.append("Please check your email for any additional instructions or access codes.\n\n");
        sb.append("Thank you for your business!\n\n");
        sb.append("Loyalty Redemption Portal Team");
        
        return sb.toString();
    }

    private String buildShippingEmail(OrderItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(item.getOrder().getAccountManager().getName()).append(",\n\n");
        sb.append("Your item has been shipped:\n\n");
        sb.append("Product: ").append(item.getProduct().getName()).append("\n");
        sb.append("SKU: ").append(item.getProduct().getSku()).append("\n");
        sb.append("Quantity: ").append(item.getQuantity()).append("\n");
        sb.append("Order: ").append(item.getOrder().getOrderNumber()).append("\n");
        sb.append("Tracking Number: ").append(item.getTrackingNumber()).append("\n\n");
        
        if (item.getOrder().getShippingAddress() != null) {
            sb.append("Shipping Address:\n").append(item.getOrder().getShippingAddress()).append("\n\n");
        }
        
        sb.append("You can track your shipment using the tracking number provided.\n\n");
        sb.append("Thank you for your business!\n\n");
        sb.append("Loyalty Redemption Portal Team");
        
        return sb.toString();
    }

    private String buildDeliveryEmail(OrderItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(item.getOrder().getAccountManager().getName()).append(",\n\n");
        sb.append("Your item has been delivered:\n\n");
        sb.append("Product: ").append(item.getProduct().getName()).append("\n");
        sb.append("SKU: ").append(item.getProduct().getSku()).append("\n");
        sb.append("Quantity: ").append(item.getQuantity()).append("\n");
        sb.append("Order: ").append(item.getOrder().getOrderNumber()).append("\n");
        sb.append("Delivered At: ").append(item.getDeliveredAt()).append("\n\n");
        
        sb.append("We hope you enjoy your new item!\n\n");
        sb.append("Thank you for your business!\n\n");
        sb.append("Loyalty Redemption Portal Team");
        
        return sb.toString();
    }

    private String buildOrderCompletionEmail(RedemptionOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(order.getAccountManager().getName()).append(",\n\n");
        sb.append("Your order has been completed:\n\n");
        sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        sb.append("Total Points: ").append(order.getTotalPoints()).append("\n");
        sb.append("Items: ").append(order.getItems().size()).append("\n");
        sb.append("Completed At: ").append(order.getCompletedAt()).append("\n\n");
        
        sb.append("All items in your order have been successfully fulfilled.\n\n");
        sb.append("Thank you for your business!\n\n");
        sb.append("Loyalty Redemption Portal Team");
        
        return sb.toString();
    }
}