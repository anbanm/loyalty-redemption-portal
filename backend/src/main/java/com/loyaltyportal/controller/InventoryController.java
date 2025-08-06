package com.loyaltyportal.controller;

import com.loyaltyportal.entity.Inventory;
import com.loyaltyportal.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventory", description = "Inventory management operations")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory status", description = "Get current inventory status for a specific product")
    public ResponseEntity<Inventory> getInventoryStatus(
            @Parameter(description = "Product ID") @PathVariable UUID productId) {
        
        return inventoryService.getInventoryStatus(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}/availability")
    @Operation(summary = "Check product availability", 
               description = "Check if sufficient quantity is available for a product")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Required quantity") @RequestParam Integer quantity) {
        
        boolean available = inventoryService.checkAvailability(productId, quantity);
        AvailabilityResponse response = new AvailabilityResponse(productId, quantity, available);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low stock levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Inventory>> getLowStockProducts() {
        List<Inventory> lowStockItems = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(lowStockItems);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock products", description = "Retrieve products that are out of stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Inventory>> getOutOfStockProducts() {
        List<Inventory> outOfStockItems = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(outOfStockItems);
    }

    @PostMapping("/product/{productId}/add-stock")
    @Operation(summary = "Add stock", description = "Add inventory stock for a product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addStock(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "Quantity to add") @RequestParam Integer quantity) {
        
        try {
            inventoryService.addStock(productId, quantity);
            logger.info("Added {} units to product: {}", quantity, productId);
            return ResponseEntity.ok("Stock added successfully");
        } catch (Exception e) {
            logger.error("Failed to add stock: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add stock: " + e.getMessage());
        }
    }

    @PostMapping("/product/{productId}/initialize")
    @Operation(summary = "Initialize inventory", description = "Initialize inventory for a new product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Inventory> initializeInventory(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Valid @RequestBody InitializeInventoryRequest request) {
        
        try {
            Inventory inventory = inventoryService.initializeInventory(
                    productId, request.getInitialQuantity(), request.getReorderPoint());
            logger.info("Initialized inventory for product: {}", productId);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            logger.error("Failed to initialize inventory: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/product/{productId}/reorder-point")
    @Operation(summary = "Update reorder point", description = "Update the reorder point for a product")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateReorderPoint(
            @Parameter(description = "Product ID") @PathVariable UUID productId,
            @Parameter(description = "New reorder point") @RequestParam Integer reorderPoint) {
        
        try {
            inventoryService.updateReorderPoint(productId, reorderPoint);
            logger.info("Updated reorder point for product: {} to {}", productId, reorderPoint);
            return ResponseEntity.ok("Reorder point updated successfully");
        } catch (Exception e) {
            logger.error("Failed to update reorder point: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to update reorder point: " + e.getMessage());
        }
    }

    @PostMapping("/batch-update")
    @Operation(summary = "Batch update inventory", description = "Update inventory levels for multiple products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> batchUpdateInventory(
            @Valid @RequestBody List<InventoryService.InventoryUpdateRequest> updates) {
        
        try {
            inventoryService.batchUpdateInventory(updates);
            logger.info("Processed batch inventory update for {} products", updates.size());
            return ResponseEntity.ok("Batch update completed successfully");
        } catch (Exception e) {
            logger.error("Failed to process batch update: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to process batch update: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get inventory statistics", description = "Retrieve inventory statistics and metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryStatistics> getInventoryStatistics() {
        Long totalInventory = inventoryService.getTotalInventoryCount();
        long outOfStockCount = inventoryService.getOutOfStockCount();
        
        InventoryStatistics stats = new InventoryStatistics(
                totalInventory != null ? totalInventory : 0,
                outOfStockCount
        );
        
        return ResponseEntity.ok(stats);
    }

    // DTOs
    public static class AvailabilityResponse {
        private UUID productId;
        private Integer requestedQuantity;
        private boolean available;

        public AvailabilityResponse(UUID productId, Integer requestedQuantity, boolean available) {
            this.productId = productId;
            this.requestedQuantity = requestedQuantity;
            this.available = available;
        }

        // Getters
        public UUID getProductId() { return productId; }
        public Integer getRequestedQuantity() { return requestedQuantity; }
        public boolean isAvailable() { return available; }
    }

    public static class InitializeInventoryRequest {
        private Integer initialQuantity;
        private Integer reorderPoint;

        // Getters and Setters
        public Integer getInitialQuantity() { return initialQuantity; }
        public void setInitialQuantity(Integer initialQuantity) { this.initialQuantity = initialQuantity; }
        public Integer getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
    }

    public static class InventoryStatistics {
        private final long totalInventoryValue;
        private final long outOfStockItems;

        public InventoryStatistics(long totalInventoryValue, long outOfStockItems) {
            this.totalInventoryValue = totalInventoryValue;
            this.outOfStockItems = outOfStockItems;
        }

        // Getters
        public long getTotalInventoryValue() { return totalInventoryValue; }
        public long getOutOfStockItems() { return outOfStockItems; }
    }
}