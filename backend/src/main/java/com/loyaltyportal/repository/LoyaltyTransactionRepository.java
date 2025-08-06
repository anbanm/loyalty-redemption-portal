package com.loyaltyportal.repository;

import com.loyaltyportal.entity.Company;
import com.loyaltyportal.entity.LoyaltyTransaction;
import com.loyaltyportal.entity.RedemptionOrder;
import com.loyaltyportal.entity.TransactionStatus;
import com.loyaltyportal.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    List<LoyaltyTransaction> findByOrder(RedemptionOrder order);

    List<LoyaltyTransaction> findByCompany(Company company);

    List<LoyaltyTransaction> findByStatus(TransactionStatus status);

    List<LoyaltyTransaction> findByTransactionType(TransactionType transactionType);

    Optional<LoyaltyTransaction> findByExternalTransactionId(String externalTransactionId);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.order.id = :orderId")
    List<LoyaltyTransaction> findByOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.company.id = :companyId")
    List<LoyaltyTransaction> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.company.id = :companyId AND lt.status = :status")
    List<LoyaltyTransaction> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") TransactionStatus status);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.company.id = :companyId AND lt.createdAt BETWEEN :startDate AND :endDate")
    List<LoyaltyTransaction> findByCompanyAndDateRange(@Param("companyId") UUID companyId, 
                                                      @Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.status = :status AND lt.retryCount < 3")
    List<LoyaltyTransaction> findRetryableTransactions(@Param("status") TransactionStatus status);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.status = 'FAILED' AND lt.retryCount < 3")
    List<LoyaltyTransaction> findFailedTransactionsForRetry();

    @Query("SELECT SUM(lt.pointsAmount) FROM LoyaltyTransaction lt WHERE lt.company.id = :companyId AND lt.transactionType = :transactionType AND lt.status = 'COMPLETED'")
    Long sumPointsByCompanyAndType(@Param("companyId") UUID companyId, @Param("transactionType") TransactionType transactionType);

    @Query("SELECT COUNT(lt) FROM LoyaltyTransaction lt WHERE lt.company.id = :companyId AND lt.status = :status")
    long countByCompanyAndStatus(@Param("companyId") UUID companyId, @Param("status") TransactionStatus status);

    @Query("SELECT COUNT(lt) FROM LoyaltyTransaction lt WHERE lt.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT lt FROM LoyaltyTransaction lt LEFT JOIN FETCH lt.order LEFT JOIN FETCH lt.company WHERE lt.id = :id")
    Optional<LoyaltyTransaction> findByIdWithOrderAndCompany(@Param("id") UUID id);

    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.status IN ('PENDING', 'FAILED') AND lt.retryCount < 3 ORDER BY lt.createdAt ASC")
    List<LoyaltyTransaction> findTransactionsToProcess();

    boolean existsByExternalTransactionId(String externalTransactionId);
}