package com.loyaltyportal.repository;

import com.loyaltyportal.entity.AccountManager;
import com.loyaltyportal.entity.Company;
import com.loyaltyportal.entity.OrderStatus;
import com.loyaltyportal.entity.RedemptionOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RedemptionOrderRepository extends JpaRepository<RedemptionOrder, UUID> {

    Optional<RedemptionOrder> findByOrderNumber(String orderNumber);

    List<RedemptionOrder> findByCompany(Company company);

    Page<RedemptionOrder> findByCompany(Company company, Pageable pageable);

    List<RedemptionOrder> findByAccountManager(AccountManager accountManager);

    Page<RedemptionOrder> findByAccountManager(AccountManager accountManager, Pageable pageable);

    List<RedemptionOrder> findByStatus(OrderStatus status);

    Page<RedemptionOrder> findByStatus(OrderStatus status, Pageable pageable);

    List<RedemptionOrder> findByCompanyAndStatus(Company company, OrderStatus status);

    List<RedemptionOrder> findByAccountManagerAndStatus(AccountManager accountManager, OrderStatus status);

    @Query("SELECT o FROM RedemptionOrder o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<RedemptionOrder> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM RedemptionOrder o WHERE o.company.id = :companyId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<RedemptionOrder> findByCompanyAndDateRange(@Param("companyId") UUID companyId, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM RedemptionOrder o WHERE o.company.id = :companyId")
    long countOrdersByCompany(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(o) FROM RedemptionOrder o WHERE o.status = :status")
    long countOrdersByStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalPoints) FROM RedemptionOrder o WHERE o.company.id = :companyId AND o.status = :status")
    Long sumPointsByCompanyAndStatus(@Param("companyId") UUID companyId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM RedemptionOrder o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<RedemptionOrder> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT o FROM RedemptionOrder o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.transactions WHERE o.id = :id")
    Optional<RedemptionOrder> findByIdWithItemsAndTransactions(@Param("id") UUID id);

    @Query("SELECT o FROM RedemptionOrder o WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<RedemptionOrder> findByStatusInOrderByCreatedAt(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM RedemptionOrder o JOIN o.items i WHERE i.product.productType = 'PHYSICAL' AND o.status = 'PROCESSING'")
    List<RedemptionOrder> findProcessingOrdersWithPhysicalItems();

    @Query("SELECT o FROM RedemptionOrder o JOIN o.items i WHERE i.product.productType = 'VIRTUAL' AND o.status = 'PROCESSING'")
    List<RedemptionOrder> findProcessingOrdersWithVirtualItems();

    boolean existsByOrderNumber(String orderNumber);
}