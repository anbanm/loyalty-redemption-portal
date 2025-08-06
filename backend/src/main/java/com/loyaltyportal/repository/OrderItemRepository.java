package com.loyaltyportal.repository;

import com.loyaltyportal.entity.FulfillmentStatus;
import com.loyaltyportal.entity.OrderItem;
import com.loyaltyportal.entity.Product;
import com.loyaltyportal.entity.RedemptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder(RedemptionOrder order);

    List<OrderItem> findByProduct(Product product);

    List<OrderItem> findByFulfillmentStatus(FulfillmentStatus fulfillmentStatus);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.company.id = :companyId")
    List<OrderItem> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.productType = 'PHYSICAL' AND oi.fulfillmentStatus = :status")
    List<OrderItem> findPhysicalItemsByFulfillmentStatus(@Param("status") FulfillmentStatus status);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.productType = 'VIRTUAL' AND oi.fulfillmentStatus = :status")
    List<OrderItem> findVirtualItemsByFulfillmentStatus(@Param("status") FulfillmentStatus status);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.fulfillmentStatus = 'PENDING' AND oi.product.productType = 'PHYSICAL'")
    List<OrderItem> findPendingPhysicalItems();

    @Query("SELECT oi FROM OrderItem oi WHERE oi.fulfillmentStatus = 'PENDING' AND oi.product.productType = 'VIRTUAL'")
    List<OrderItem> findPendingVirtualItems();

    @Query("SELECT oi FROM OrderItem oi WHERE oi.trackingNumber = :trackingNumber")
    List<OrderItem> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    @Query("SELECT SUM(oi.quantity * oi.pointsPerItem) FROM OrderItem oi WHERE oi.order.company.id = :companyId")
    Long sumTotalPointsByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.fulfillmentStatus = :status")
    long countByFulfillmentStatus(@Param("status") FulfillmentStatus status);

    @Query("SELECT p.name, SUM(oi.quantity) as totalQuantity FROM OrderItem oi JOIN oi.product p " +
           "GROUP BY p.id, p.name ORDER BY totalQuantity DESC")
    List<Object[]> findPopularProducts();
}