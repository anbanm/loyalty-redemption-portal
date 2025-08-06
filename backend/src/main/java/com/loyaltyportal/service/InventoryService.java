package com.loyaltyportal.service;

import com.loyaltyportal.entity.Inventory;
import com.loyaltyportal.entity.Product;
import com.loyaltyportal.repository.InventoryRepository;
import com.loyaltyportal.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Check if sufficient inventory is available for a product
     */
    public boolean checkAvailability(UUID productId, Integer requiredQuantity) {
        logger.debug("Checking availability for product: {} quantity: {}", productId, requiredQuantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (inventoryOpt.isEmpty()) {
            logger.warn("No inventory record found for product: {}", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        boolean available = inventory.canReserve(requiredQuantity);
        
        logger.debug("Product {} availability check: {} (available: {}, required: {})", 
                productId, available, inventory.getQuantityAvailable(), requiredQuantity);
        
        return available;
    }

    /**
     * Reserve inventory for an order
     */
    public void reserveInventory(UUID productId, Integer quantity) {
        logger.info("Reserving {} units of product: {}", quantity, productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        try {
            inventory.reserveQuantity(quantity);
            inventoryRepository.save(inventory);
            
            logger.info("Successfully reserved {} units of product: {} (available: {}, reserved: {})", 
                    quantity, productId, inventory.getQuantityAvailable(), inventory.getQuantityReserved());
            
            // Check if inventory is now low
            if (inventory.isLowStock()) {
                logger.warn("Low stock alert for product: {} (available: {}, reorder point: {})", 
                        productId, inventory.getQuantityAvailable(), inventory.getReorderPoint());
            }
            
        } catch (IllegalStateException e) {
            logger.error("Failed to reserve inventory for product {}: {}", productId, e.getMessage());
            throw new InventoryException("Cannot reserve inventory: " + e.getMessage());
        }
    }

    /**
     * Release reserved inventory (in case of order cancellation)
     */
    public void releaseReservation(UUID productId, Integer quantity) {
        logger.info("Releasing reservation of {} units for product: {}", quantity, productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        try {
            inventory.releaseReserved(quantity);
            inventoryRepository.save(inventory);
            
            logger.info("Successfully released {} units for product: {} (available: {}, reserved: {})", 
                    quantity, productId, inventory.getQuantityAvailable(), inventory.getQuantityReserved());
            
        } catch (IllegalStateException e) {
            logger.error("Failed to release reservation for product {}: {}", productId, e.getMessage());
            throw new InventoryException("Cannot release reservation: " + e.getMessage());
        }
    }

    /**
     * Confirm reserved inventory (when order is fulfilled)
     */
    public void confirmReservation(UUID productId, Integer quantity) {
        logger.info("Confirming reservation of {} units for product: {}", quantity, productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        try {
            inventory.confirmReserved(quantity);
            inventoryRepository.save(inventory);
            
            logger.info("Successfully confirmed {} units for product: {} (available: {}, reserved: {})", 
                    quantity, productId, inventory.getQuantityAvailable(), inventory.getQuantityReserved());
            
        } catch (IllegalStateException e) {
            logger.error("Failed to confirm reservation for product {}: {}", productId, e.getMessage());
            throw new InventoryException("Cannot confirm reservation: " + e.getMessage());
        }
    }

    /**
     * Add stock to inventory
     */
    public void addStock(UUID productId, Integer quantity) {
        logger.info("Adding {} units to product: {}", quantity, productId);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        try {
            inventory.addStock(quantity);
            inventoryRepository.save(inventory);
            
            logger.info("Successfully added {} units to product: {} (total available: {})", 
                    quantity, productId, inventory.getQuantityAvailable());
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to add stock for product {}: {}", productId, e.getMessage());
            throw new InventoryException("Cannot add stock: " + e.getMessage());
        }
    }

    /**
     * Get current inventory status for a product
     */
    public Optional<Inventory> getInventoryStatus(UUID productId) {
        return inventoryRepository.findByProductIdWithProduct(productId);
    }

    /**
     * Get all products with low stock
     */
    public List<Inventory> getLowStockProducts() {
        return inventoryRepository.findLowStockItems();
    }

    /**
     * Get all products that are out of stock
     */
    public List<Inventory> getOutOfStockProducts() {
        return inventoryRepository.findAllInStock()
                .stream()
                .filter(inventory -> inventory.getQuantityAvailable() == 0)
                .toList();
    }

    /**
     * Initialize inventory for a new product
     */
    public Inventory initializeInventory(UUID productId, Integer initialQuantity, Integer reorderPoint) {
        logger.info("Initializing inventory for product: {} with {} units", productId, initialQuantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new InventoryException("Product not found: " + productId));
        
        // Check if inventory already exists
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new InventoryException("Inventory already exists for product: " + productId);
        }
        
        Inventory inventory = new Inventory(product, initialQuantity);
        inventory.setReorderPoint(reorderPoint);
        
        inventory = inventoryRepository.save(inventory);
        
        logger.info("Successfully initialized inventory for product: {} (available: {}, reorder point: {})", 
                productId, inventory.getQuantityAvailable(), inventory.getReorderPoint());
        
        return inventory;
    }

    /**
     * Update reorder point for a product
     */
    public void updateReorderPoint(UUID productId, Integer newReorderPoint) {
        logger.info("Updating reorder point for product: {} to {}", productId, newReorderPoint);
        
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        inventory.setReorderPoint(newReorderPoint);
        inventoryRepository.save(inventory);
        
        logger.info("Successfully updated reorder point for product: {} to {}", productId, newReorderPoint);
    }

    /**
     * Get total inventory value across all products
     */
    public Long getTotalInventoryCount() {
        return inventoryRepository.getTotalInventoryValue();
    }

    /**
     * Get count of out of stock items
     */
    public long getOutOfStockCount() {
        return inventoryRepository.countOutOfStockItems();
    }

    /**
     * Batch update inventory levels (for admin operations)
     */
    @Transactional
    public void batchUpdateInventory(List<InventoryUpdateRequest> updates) {
        logger.info("Processing batch inventory update for {} products", updates.size());
        
        for (InventoryUpdateRequest update : updates) {
            try {
                if (update.getOperation() == InventoryOperation.ADD) {
                    addStock(update.getProductId(), update.getQuantity());
                } else if (update.getOperation() == InventoryOperation.SET) {
                    setInventoryLevel(update.getProductId(), update.getQuantity());
                }
            } catch (Exception e) {
                logger.error("Failed to update inventory for product {}: {}", 
                        update.getProductId(), e.getMessage());
                // Continue with other updates rather than failing the entire batch
            }
        }
    }

    private void setInventoryLevel(UUID productId, Integer newLevel) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("No inventory record found for product: " + productId));
        
        inventory.setQuantityAvailable(newLevel);
        inventoryRepository.save(inventory);
    }

    // Helper classes and enums
    public static class InventoryUpdateRequest {
        private UUID productId;
        private Integer quantity;
        private InventoryOperation operation;

        public InventoryUpdateRequest() {}

        public InventoryUpdateRequest(UUID productId, Integer quantity, InventoryOperation operation) {
            this.productId = productId;
            this.quantity = quantity;
            this.operation = operation;
        }

        // Getters and Setters
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public InventoryOperation getOperation() { return operation; }
        public void setOperation(InventoryOperation operation) { this.operation = operation; }
    }

    public enum InventoryOperation {
        ADD, SET
    }

    public static class InventoryException extends RuntimeException {
        public InventoryException(String message) {
            super(message);
        }
        
        public InventoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}