package com.loyaltyportal.service;

import com.loyaltyportal.entity.*;
import com.loyaltyportal.repository.LoyaltyTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoyaltyTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyTransactionService.class);

    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyApiClient loyaltyApiClient;

    @Autowired
    public LoyaltyTransactionService(LoyaltyTransactionRepository transactionRepository, 
                                   LoyaltyApiClient loyaltyApiClient) {
        this.transactionRepository = transactionRepository;
        this.loyaltyApiClient = loyaltyApiClient;
    }

    /**
     * Record a successful transaction
     */
    public LoyaltyTransaction recordTransaction(RedemptionOrder order, TransactionType type, 
                                             Integer points, String externalTransactionId, 
                                             TransactionStatus status) {
        logger.info("Recording {} transaction for order: {} amount: {} points", 
                type, order.getOrderNumber(), points);
        
        LoyaltyTransaction transaction = new LoyaltyTransaction(order, order.getCompany(), points, type);
        transaction.setExternalTransactionId(externalTransactionId);
        transaction.setStatus(status);
        
        if (TransactionStatus.COMPLETED.equals(status)) {
            transaction.setProcessedAt(LocalDateTime.now());
        }
        
        transaction = transactionRepository.save(transaction);
        
        logger.info("Successfully recorded transaction: {} for order: {}", 
                transaction.getId(), order.getOrderNumber());
        
        return transaction;
    }

    /**
     * Record a failed transaction
     */
    public LoyaltyTransaction recordFailedTransaction(RedemptionOrder order, TransactionType type, 
                                                    Integer points, String errorMessage) {
        logger.warn("Recording failed {} transaction for order: {} error: {}", 
                type, order.getOrderNumber(), errorMessage);
        
        LoyaltyTransaction transaction = new LoyaltyTransaction(order, order.getCompany(), points, type);
        transaction.markAsFailed(errorMessage);
        
        transaction = transactionRepository.save(transaction);
        
        logger.warn("Recorded failed transaction: {} for order: {}", 
                transaction.getId(), order.getOrderNumber());
        
        return transaction;
    }

    /**
     * Get all transactions for a company
     */
    public List<LoyaltyTransaction> getTransactionsByCompany(UUID companyId) {
        return transactionRepository.findByCompanyId(companyId);
    }

    /**
     * Get all transactions for an order
     */
    public List<LoyaltyTransaction> getTransactionsByOrder(UUID orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    /**
     * Get transactions by status
     */
    public List<LoyaltyTransaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    /**
     * Get failed transactions that can be retried
     */
    public List<LoyaltyTransaction> getRetryableTransactions() {
        return transactionRepository.findFailedTransactionsForRetry();
    }

    /**
     * Retry a failed transaction
     */
    public void retryTransaction(UUID transactionId) {
        logger.info("Retrying transaction: {}", transactionId);
        
        Optional<LoyaltyTransaction> transactionOpt = transactionRepository.findByIdWithOrderAndCompany(transactionId);
        if (transactionOpt.isEmpty()) {
            logger.error("Transaction not found for retry: {}", transactionId);
            throw new TransactionException("Transaction not found: " + transactionId);
        }
        
        LoyaltyTransaction transaction = transactionOpt.get();
        
        if (!transaction.canRetry()) {
            logger.error("Transaction cannot be retried: {} (status: {}, retry count: {})", 
                    transactionId, transaction.getStatus(), transaction.getRetryCount());
            throw new TransactionException("Transaction cannot be retried");
        }
        
        transaction.incrementRetryCount();
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);
        
        // Execute the retry based on transaction type
        try {
            executeTransactionRetry(transaction);
        } catch (Exception e) {
            logger.error("Retry failed for transaction {}: {}", transactionId, e.getMessage());
            transaction.markAsFailed("Retry failed: " + e.getMessage());
            transactionRepository.save(transaction);
            throw new TransactionException("Retry failed: " + e.getMessage());
        }
    }

    /**
     * Process pending transactions (scheduled job)
     */
    @Transactional
    public void processPendingTransactions() {
        List<LoyaltyTransaction> pendingTransactions = transactionRepository.findTransactionsToProcess();
        
        logger.info("Processing {} pending transactions", pendingTransactions.size());
        
        for (LoyaltyTransaction transaction : pendingTransactions) {
            try {
                processTransaction(transaction);
            } catch (Exception e) {
                logger.error("Failed to process transaction {}: {}", transaction.getId(), e.getMessage());
                transaction.incrementRetryCount();
                if (transaction.getRetryCount() >= 3) {
                    transaction.markAsFailed("Max retries exceeded: " + e.getMessage());
                }
                transactionRepository.save(transaction);
            }
        }
    }

    /**
     * Calculate total points used by a company
     */
    public Long getTotalPointsUsed(UUID companyId) {
        return transactionRepository.sumPointsByCompanyAndType(companyId, TransactionType.DEBIT);
    }

    /**
     * Calculate total points refunded to a company
     */
    public Long getTotalPointsRefunded(UUID companyId) {
        return transactionRepository.sumPointsByCompanyAndType(companyId, TransactionType.REFUND);
    }

    /**
     * Get transaction statistics
     */
    public TransactionStatistics getTransactionStatistics() {
        long totalTransactions = transactionRepository.count();
        long pendingCount = transactionRepository.countByStatus(TransactionStatus.PENDING);
        long processingCount = transactionRepository.countByStatus(TransactionStatus.PROCESSING);
        long completedCount = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long failedCount = transactionRepository.countByStatus(TransactionStatus.FAILED);
        long refundedCount = transactionRepository.countByStatus(TransactionStatus.REFUNDED);
        
        return new TransactionStatistics(totalTransactions, pendingCount, processingCount, 
                completedCount, failedCount, refundedCount);
    }

    // Private helper methods
    private void executeTransactionRetry(LoyaltyTransaction transaction) {
        Company company = transaction.getCompany();
        String reference = generateTransactionReference(transaction);
        
        if (TransactionType.DEBIT.equals(transaction.getTransactionType())) {
            // Retry debit
            loyaltyApiClient.debitPoints(company.getLoyaltyAccountId(), 
                    transaction.getPointsAmount(), reference)
                    .subscribe(
                        response -> handleSuccessfulRetry(transaction, response.getTransactionId()),
                        error -> handleFailedRetry(transaction, error.getMessage())
                    );
        } else if (TransactionType.CREDIT.equals(transaction.getTransactionType()) || 
                   TransactionType.REFUND.equals(transaction.getTransactionType())) {
            // Retry credit/refund
            loyaltyApiClient.creditPoints(company.getLoyaltyAccountId(), 
                    transaction.getPointsAmount(), reference)
                    .subscribe(
                        response -> handleSuccessfulRetry(transaction, response.getTransactionId()),
                        error -> handleFailedRetry(transaction, error.getMessage())
                    );
        }
    }

    private void processTransaction(LoyaltyTransaction transaction) {
        transaction.markAsProcessing();
        transactionRepository.save(transaction);
        
        executeTransactionRetry(transaction);
    }

    private void handleSuccessfulRetry(LoyaltyTransaction transaction, String externalTransactionId) {
        transaction.markAsCompleted(externalTransactionId);
        transactionRepository.save(transaction);
        
        logger.info("Successfully retried transaction: {} with external ID: {}", 
                transaction.getId(), externalTransactionId);
    }

    private void handleFailedRetry(LoyaltyTransaction transaction, String errorMessage) {
        transaction.markAsFailed("Retry failed: " + errorMessage);
        transactionRepository.save(transaction);
        
        logger.error("Failed to retry transaction: {} error: {}", transaction.getId(), errorMessage);
    }

    private String generateTransactionReference(LoyaltyTransaction transaction) {
        String prefix = transaction.getTransactionType().name();
        String orderNumber = transaction.getOrder().getOrderNumber();
        String suffix = transaction.getRetryCount() > 0 ? "-RETRY-" + transaction.getRetryCount() : "";
        
        return prefix + "-" + orderNumber + suffix;
    }

    // Helper classes
    public static class TransactionStatistics {
        private final long totalTransactions;
        private final long pendingCount;
        private final long processingCount;
        private final long completedCount;
        private final long failedCount;
        private final long refundedCount;

        public TransactionStatistics(long totalTransactions, long pendingCount, long processingCount, 
                                   long completedCount, long failedCount, long refundedCount) {
            this.totalTransactions = totalTransactions;
            this.pendingCount = pendingCount;
            this.processingCount = processingCount;
            this.completedCount = completedCount;
            this.failedCount = failedCount;
            this.refundedCount = refundedCount;
        }

        // Getters
        public long getTotalTransactions() { return totalTransactions; }
        public long getPendingCount() { return pendingCount; }
        public long getProcessingCount() { return processingCount; }
        public long getCompletedCount() { return completedCount; }
        public long getFailedCount() { return failedCount; }
        public long getRefundedCount() { return refundedCount; }
        
        public double getSuccessRate() {
            return totalTransactions > 0 ? (double) completedCount / totalTransactions * 100 : 0;
        }
    }

    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }
        
        public TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}