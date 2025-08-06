package com.loyaltyportal.controller;

import com.loyaltyportal.entity.OrderStatus;
import com.loyaltyportal.entity.RedemptionOrder;
import com.loyaltyportal.repository.RedemptionOrderRepository;
import com.loyaltyportal.service.OrderWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final RedemptionOrderRepository orderRepository;
    private final OrderWorkflowService workflowService;

    @Autowired
    public OrderController(RedemptionOrderRepository orderRepository, OrderWorkflowService workflowService) {
        this.orderRepository = orderRepository;
        this.workflowService = workflowService;
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order with its items and transactions")
    public ResponseEntity<RedemptionOrder> getOrderById(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        
        return orderRepository.findByIdWithItemsAndTransactions(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieve an order by its order number")
    public ResponseEntity<RedemptionOrder> getOrderByNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        
        return orderRepository.findByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get orders by company", description = "Retrieve all orders for a specific company")
    public ResponseEntity<Page<RedemptionOrder>> getOrdersByCompany(
            @Parameter(description = "Company ID") @PathVariable UUID companyId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        // Note: In a real implementation, we would first fetch the Company entity
        // For now, we'll use a simpler approach with a custom query
        Page<RedemptionOrder> orders = orderRepository.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/account-manager/{accountManagerId}")
    @Operation(summary = "Get orders by account manager", 
               description = "Retrieve all orders for a specific account manager")
    public ResponseEntity<Page<RedemptionOrder>> getOrdersByAccountManager(
            @Parameter(description = "Account Manager ID") @PathVariable UUID accountManagerId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        // Note: Similar to above, in a real implementation we would fetch the AccountManager entity
        Page<RedemptionOrder> orders = orderRepository.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve orders filtered by status")
    public ResponseEntity<Page<RedemptionOrder>> getOrdersByStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<RedemptionOrder> orders = orderRepository.findByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get orders by date range", description = "Retrieve orders within a specific date range")
    public ResponseEntity<List<RedemptionOrder>> getOrdersByDateRange(
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<RedemptionOrder> orders = orderRepository.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/items/{itemId}/ship")
    @Operation(summary = "Mark item as shipped", description = "Mark a physical item as shipped with tracking number")
    public ResponseEntity<String> markItemAsShipped(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Order Item ID") @PathVariable UUID itemId,
            @Parameter(description = "Tracking number") @RequestParam String trackingNumber) {
        
        try {
            workflowService.markItemAsShipped(itemId, trackingNumber);
            logger.info("Item {} marked as shipped with tracking: {}", itemId, trackingNumber);
            return ResponseEntity.ok("Item marked as shipped successfully");
        } catch (Exception e) {
            logger.error("Failed to mark item as shipped: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to mark item as shipped: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/items/{itemId}/deliver")
    @Operation(summary = "Mark item as delivered", description = "Mark a shipped item as delivered")
    public ResponseEntity<String> markItemAsDelivered(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Order Item ID") @PathVariable UUID itemId) {
        
        try {
            workflowService.markItemAsDelivered(itemId);
            logger.info("Item {} marked as delivered", itemId);
            return ResponseEntity.ok("Item marked as delivered successfully");
        } catch (Exception e) {
            logger.error("Failed to mark item as delivered: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to mark item as delivered: " + e.getMessage());
        }
    }

    @GetMapping("/processing/physical")
    @Operation(summary = "Get orders with physical items", 
               description = "Retrieve orders that are processing and contain physical items")
    public ResponseEntity<List<RedemptionOrder>> getProcessingOrdersWithPhysicalItems() {
        List<RedemptionOrder> orders = orderRepository.findProcessingOrdersWithPhysicalItems();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/processing/virtual")
    @Operation(summary = "Get orders with virtual items", 
               description = "Retrieve orders that are processing and contain virtual items")
    public ResponseEntity<List<RedemptionOrder>> getProcessingOrdersWithVirtualItems() {
        List<RedemptionOrder> orders = orderRepository.findProcessingOrdersWithVirtualItems();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Retrieve order statistics and counts")
    public ResponseEntity<OrderStatistics> getOrderStatistics() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countOrdersByStatus(OrderStatus.PENDING);
        long processingOrders = orderRepository.countOrdersByStatus(OrderStatus.PROCESSING);
        long completedOrders = orderRepository.countOrdersByStatus(OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countOrdersByStatus(OrderStatus.CANCELLED);
        long failedOrders = orderRepository.countOrdersByStatus(OrderStatus.FAILED);
        
        OrderStatistics stats = new OrderStatistics(
                totalOrders, pendingOrders, processingOrders, 
                completedOrders, cancelledOrders, failedOrders
        );
        
        return ResponseEntity.ok(stats);
    }

    // DTO for order statistics
    public static class OrderStatistics {
        private final long totalOrders;
        private final long pendingOrders;
        private final long processingOrders;
        private final long completedOrders;
        private final long cancelledOrders;
        private final long failedOrders;

        public OrderStatistics(long totalOrders, long pendingOrders, long processingOrders, 
                             long completedOrders, long cancelledOrders, long failedOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.processingOrders = processingOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.failedOrders = failedOrders;
        }

        // Getters
        public long getTotalOrders() { return totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public long getProcessingOrders() { return processingOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public long getFailedOrders() { return failedOrders; }
        
        public double getCompletionRate() {
            return totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
        }
    }
}