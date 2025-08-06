package com.loyaltyportal.service;

import com.loyaltyportal.entity.*;
import com.loyaltyportal.repository.OrderItemRepository;
import com.loyaltyportal.repository.RedemptionOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(OrderWorkflowService.class);

    private final RedemptionOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final WebClient virtualFulfillmentClient;

    @Value("${loyalty.fulfillment.virtual.api-url}")
    private String virtualFulfillmentApiUrl;

    @Autowired
    public OrderWorkflowService(
            RedemptionOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            InventoryService inventoryService,
            NotificationService notificationService,
            WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.virtualFulfillmentClient = webClientBuilder.build();
    }

    /**
     * Initiate fulfillment workflows for an order
     */
    public void initiateOrderFulfillment(RedemptionOrder order) {
        logger.info("Initiating fulfillment workflows for order: {}", order.getOrderNumber());
        
        // Process physical items
        List<OrderItem> physicalItems = order.getItems().stream()
                .filter(OrderItem::isPhysicalProduct)
                .toList();
        
        if (!physicalItems.isEmpty()) {
            processPhysicalItems(order, physicalItems);
        }
        
        // Process virtual items
        List<OrderItem> virtualItems = order.getItems().stream()
                .filter(OrderItem::isVirtualProduct)
                .toList();
        
        if (!virtualItems.isEmpty()) {
            processVirtualItems(order, virtualItems);
        }
        
        // Send order confirmation notification
        notificationService.sendOrderConfirmation(order);
    }

    /**
     * Process physical items - create manual fulfillment tasks
     */
    private void processPhysicalItems(RedemptionOrder order, List<OrderItem> physicalItems) {
        logger.info("Processing {} physical items for order: {}", physicalItems.size(), order.getOrderNumber());
        
        for (OrderItem item : physicalItems) {
            try {
                // Confirm inventory reservation
                inventoryService.confirmReservation(item.getProduct().getId(), item.getQuantity());
                
                // Mark item as pending fulfillment
                item.setFulfillmentStatus(FulfillmentStatus.PROCESSING);
                orderItemRepository.save(item);
                
                // Create manual fulfillment task
                createManualFulfillmentTask(order, item);
                
                logger.info("Created manual fulfillment task for item: {} in order: {}", 
                        item.getProduct().getSku(), order.getOrderNumber());
                
            } catch (Exception e) {
                logger.error("Failed to process physical item {} for order {}: {}", 
                        item.getProduct().getSku(), order.getOrderNumber(), e.getMessage());
                
                item.markAsFailed("Failed to initiate fulfillment: " + e.getMessage());
                orderItemRepository.save(item);
            }
        }
        
        // Send notification to fulfillment team
        notificationService.sendPhysicalFulfillmentNotification(order, physicalItems);
    }

    /**
     * Process virtual items - trigger API fulfillment
     */
    private void processVirtualItems(RedemptionOrder order, List<OrderItem> virtualItems) {
        logger.info("Processing {} virtual items for order: {}", virtualItems.size(), order.getOrderNumber());
        
        for (OrderItem item : virtualItems) {
            try {
                // Mark item as processing
                item.setFulfillmentStatus(FulfillmentStatus.PROCESSING);
                orderItemRepository.save(item);
                
                // Trigger virtual fulfillment
                fulfillVirtualItem(order, item);
                
            } catch (Exception e) {
                logger.error("Failed to process virtual item {} for order {}: {}", 
                        item.getProduct().getSku(), order.getOrderNumber(), e.getMessage());
                
                item.markAsFailed("Failed to fulfill virtually: " + e.getMessage());
                orderItemRepository.save(item);
            }
        }
    }

    /**
     * Fulfill a virtual item via API call
     */
    private void fulfillVirtualItem(RedemptionOrder order, OrderItem item) {
        logger.info("Fulfilling virtual item: {} for order: {}", 
                item.getProduct().getSku(), order.getOrderNumber());
        
        VirtualFulfillmentRequest request = new VirtualFulfillmentRequest(
                order.getOrderNumber() + "-" + item.getId(),
                item.getProduct().getSku(),
                item.getQuantity(),
                order.getAccountManager().getEmail(),
                order.getAccountManager().getName(),
                order.getCompany().getName()
        );
        
        virtualFulfillmentClient.post()
                .uri(virtualFulfillmentApiUrl + "/fulfill")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VirtualFulfillmentResponse.class)
                .subscribe(
                    response -> handleVirtualFulfillmentSuccess(item, response),
                    error -> handleVirtualFulfillmentError(item, error)
                );
    }

    /**
     * Handle successful virtual fulfillment
     */
    private void handleVirtualFulfillmentSuccess(OrderItem item, VirtualFulfillmentResponse response) {
        logger.info("Virtual fulfillment successful for item: {} with reference: {}", 
                item.getProduct().getSku(), response.getFulfillmentId());
        
        item.markAsFulfilled(response.getFulfillmentId());
        item.setDeliveredAt(LocalDateTime.now());
        orderItemRepository.save(item);
        
        // Check if all items in the order are fulfilled
        checkOrderCompletion(item.getOrder());
        
        // Send fulfillment notification
        notificationService.sendVirtualFulfillmentNotification(item, response);
    }

    /**
     * Handle virtual fulfillment error
     */
    private void handleVirtualFulfillmentError(OrderItem item, Throwable error) {
        logger.error("Virtual fulfillment failed for item: {} error: {}", 
                item.getProduct().getSku(), error.getMessage());
        
        item.markAsFailed("Virtual fulfillment failed: " + error.getMessage());
        orderItemRepository.save(item);
        
        // Notify about the failure
        notificationService.sendFulfillmentFailureNotification(item, error.getMessage());
    }

    /**
     * Mark physical item as shipped
     */
    public void markItemAsShipped(UUID itemId, String trackingNumber) {
        logger.info("Marking item as shipped: {} tracking: {}", itemId, trackingNumber);
        
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new WorkflowException("Order item not found: " + itemId));
        
        if (!item.isPhysicalProduct()) {
            throw new WorkflowException("Cannot ship virtual product");
        }
        
        item.markAsShipped(trackingNumber);
        orderItemRepository.save(item);
        
        // Send shipping notification
        notificationService.sendShippingNotification(item);
        
        logger.info("Successfully marked item as shipped: {} tracking: {}", itemId, trackingNumber);
    }

    /**
     * Mark physical item as delivered
     */
    public void markItemAsDelivered(UUID itemId) {
        logger.info("Marking item as delivered: {}", itemId);
        
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new WorkflowException("Order item not found: " + itemId));
        
        if (!FulfillmentStatus.SHIPPED.equals(item.getFulfillmentStatus())) {
            throw new WorkflowException("Item must be shipped before it can be delivered");
        }
        
        item.markAsDelivered();
        orderItemRepository.save(item);
        
        // Check if all items in the order are completed
        checkOrderCompletion(item.getOrder());
        
        // Send delivery notification
        notificationService.sendDeliveryNotification(item);
        
        logger.info("Successfully marked item as delivered: {}", itemId);
    }

    /**
     * Check if order is complete and update status
     */
    private void checkOrderCompletion(RedemptionOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        
        boolean allItemsCompleted = items.stream().allMatch(item -> 
                FulfillmentStatus.DELIVERED.equals(item.getFulfillmentStatus()) ||
                FulfillmentStatus.FULFILLED.equals(item.getFulfillmentStatus()));
        
        if (allItemsCompleted && !order.isCompleted()) {
            order.markAsCompleted();
            orderRepository.save(order);
            
            // Send order completion notification
            notificationService.sendOrderCompletionNotification(order);
            
            logger.info("Order completed: {}", order.getOrderNumber());
        }
    }

    /**
     * Get orders that need manual fulfillment attention
     */
    public List<OrderItem> getItemsPendingManualFulfillment() {
        return orderItemRepository.findPendingPhysicalItems();
    }

    /**
     * Get items that failed fulfillment
     */
    public List<OrderItem> getFailedFulfillmentItems() {
        return orderItemRepository.findByFulfillmentStatus(FulfillmentStatus.FAILED);
    }

    // Private helper methods
    private void createManualFulfillmentTask(RedemptionOrder order, OrderItem item) {
        // In a real implementation, this would create a task in a workflow system
        // or send to a fulfillment management system
        logger.info("Creating manual fulfillment task for order: {} item: {} quantity: {}", 
                order.getOrderNumber(), item.getProduct().getSku(), item.getQuantity());
        
        // For now, we'll just log the task creation
        // Future enhancement: integrate with workflow management system
    }

    // DTOs for virtual fulfillment API
    private static class VirtualFulfillmentRequest {
        private String referenceId;
        private String productSku;
        private Integer quantity;
        private String customerEmail;
        private String customerName;
        private String companyName;

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

    private static class VirtualFulfillmentResponse {
        private String fulfillmentId;
        private String status;
        private String message;

        public VirtualFulfillmentResponse() {}

        public String getFulfillmentId() { return fulfillmentId; }
        public void setFulfillmentId(String fulfillmentId) { this.fulfillmentId = fulfillmentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class WorkflowException extends RuntimeException {
        public WorkflowException(String message) {
            super(message);
        }
        
        public WorkflowException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}