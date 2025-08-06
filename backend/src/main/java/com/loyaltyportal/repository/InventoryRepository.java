package com.loyaltyportal.repository;

import com.loyaltyportal.entity.Inventory;
import com.loyaltyportal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProduct(Product product);

    Optional<Inventory> findByProductId(UUID productId);

    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable > 0")
    List<Inventory> findAllInStock();

    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.reorderPoint AND i.reorderPoint IS NOT NULL")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE i.quantityReserved > 0")
    List<Inventory> findAllWithReservations();

    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable >= :requiredQuantity")
    List<Inventory> findAvailableForQuantity(@Param("requiredQuantity") Integer requiredQuantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityAvailable = i.quantityAvailable - :quantity, " +
           "i.quantityReserved = i.quantityReserved + :quantity, " +
           "i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND i.quantityAvailable >= :quantity")
    int reserveQuantity(@Param("productId") UUID productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityReserved = i.quantityReserved - :quantity, " +
           "i.quantityAvailable = i.quantityAvailable + :quantity, " +
           "i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND i.quantityReserved >= :quantity")
    int releaseReservation(@Param("productId") UUID productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantityReserved = i.quantityReserved - :quantity, " +
           "i.lastUpdated = CURRENT_TIMESTAMP WHERE i.product.id = :productId AND i.quantityReserved >= :quantity")
    int confirmReservation(@Param("productId") UUID productId, @Param("quantity") Integer quantity);

    @Query("SELECT i FROM Inventory i LEFT JOIN FETCH i.product WHERE i.product.id = :productId")
    Optional<Inventory> findByProductIdWithProduct(@Param("productId") UUID productId);

    @Query("SELECT SUM(i.quantityAvailable + i.quantityReserved) FROM Inventory i")
    Long getTotalInventoryValue();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable = 0")
    long countOutOfStockItems();
}