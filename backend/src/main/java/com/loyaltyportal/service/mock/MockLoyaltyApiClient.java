package com.loyaltyportal.service.mock;

import com.loyaltyportal.dto.BalanceResponse;
import com.loyaltyportal.dto.TransactionRequest;
import com.loyaltyportal.dto.TransactionResponse;
import com.loyaltyportal.service.LoyaltyApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "loyalty.api.mock.enabled", havingValue = "true")
public class MockLoyaltyApiClient extends LoyaltyApiClient {

    private static final Logger logger = LoggerFactory.getLogger(MockLoyaltyApiClient.class);

    // Mock data storage
    private final Map<String, Integer> accountBalances = new HashMap<>();
    private final Map<String, String> accountTiers = new HashMap<>();

    public MockLoyaltyApiClient() {
        super("http://mock-api", "mock-key", null, 3, null);
        initializeMockData();
    }

    private void initializeMockData() {
        // Initialize some mock account data
        accountBalances.put("ACME001", 150000);
        accountBalances.put("GLOBAL002", 75000);
        accountBalances.put("TECH003", 200000);
        accountBalances.put("STARTUP004", 25000);
        
        accountTiers.put("ACME001", "GOLD");
        accountTiers.put("GLOBAL002", "SILVER");
        accountTiers.put("TECH003", "PLATINUM");
        accountTiers.put("STARTUP004", "BRONZE");
        
        logger.info("Mock Loyalty API Client initialized with {} accounts", accountBalances.size());
    }

    @Override
    public Mono<BalanceResponse> getBalance(String loyaltyAccountId) {
        logger.info("MOCK: Fetching balance for loyalty account: {}", loyaltyAccountId);
        
        return Mono.fromSupplier(() -> {
            Integer balance = accountBalances.getOrDefault(loyaltyAccountId, 50000); // Default balance
            String tier = accountTiers.getOrDefault(loyaltyAccountId, "SILVER");
            
            // Simulate some processing time
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            BalanceResponse response = new BalanceResponse(
                loyaltyAccountId,
                balance,
                balance - 1000, // available balance (reserved 1000 points)
                1000, // pending balance
                LocalDateTime.now().minusHours(2),
                tier
            );
            
            logger.info("MOCK: Balance response for {}: {} points", loyaltyAccountId, balance);
            return response;
        });
    }

    @Override
    public Mono<TransactionResponse> debitPoints(String loyaltyAccountId, Integer points, String reference) {
        logger.info("MOCK: Debiting {} points from loyalty account: {} with reference: {}", 
                points, loyaltyAccountId, reference);
        
        return Mono.fromSupplier(() -> {
            Integer currentBalance = accountBalances.getOrDefault(loyaltyAccountId, 50000);
            
            // Simulate business rules
            if (points > currentBalance) {
                TransactionResponse errorResponse = new TransactionResponse();
                errorResponse.setTransactionId(null);
                errorResponse.setAccountId(loyaltyAccountId);
                errorResponse.setPoints(points);
                errorResponse.setReference(reference);
                errorResponse.setStatus("FAILED");
                errorResponse.setErrorCode("INSUFFICIENT_BALANCE");
                errorResponse.setErrorMessage("Insufficient points balance");
                errorResponse.setProcessedAt(LocalDateTime.now());
                
                logger.warn("MOCK: Debit failed - insufficient balance for account: {}", loyaltyAccountId);
                return errorResponse;
            }
            
            // Simulate occasional failures (5% failure rate)
            if (Math.random() < 0.05) {
                TransactionResponse errorResponse = new TransactionResponse();
                errorResponse.setTransactionId(null);
                errorResponse.setAccountId(loyaltyAccountId);
                errorResponse.setPoints(points);
                errorResponse.setReference(reference);
                errorResponse.setStatus("FAILED");
                errorResponse.setErrorCode("SYSTEM_ERROR");
                errorResponse.setErrorMessage("Temporary system error - please retry");
                errorResponse.setProcessedAt(LocalDateTime.now());
                
                logger.warn("MOCK: Simulated system error for account: {}", loyaltyAccountId);
                return errorResponse;
            }
            
            // Process successful debit
            Integer newBalance = currentBalance - points;
            accountBalances.put(loyaltyAccountId, newBalance);
            
            // Simulate processing time
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            TransactionResponse response = new TransactionResponse();
            response.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8));
            response.setAccountId(loyaltyAccountId);
            response.setPoints(points);
            response.setReference(reference);
            response.setStatus("SUCCESS");
            response.setBalanceBefore(currentBalance);
            response.setBalanceAfter(newBalance);
            response.setProcessedAt(LocalDateTime.now());
            
            logger.info("MOCK: Successfully debited {} points from {}, new balance: {}", 
                    points, loyaltyAccountId, newBalance);
            
            return response;
        });
    }

    @Override
    public Mono<TransactionResponse> creditPoints(String loyaltyAccountId, Integer points, String reference) {
        logger.info("MOCK: Crediting {} points to loyalty account: {} with reference: {}", 
                points, loyaltyAccountId, reference);
        
        return Mono.fromSupplier(() -> {
            Integer currentBalance = accountBalances.getOrDefault(loyaltyAccountId, 50000);
            
            // Simulate occasional failures (2% failure rate for credits)
            if (Math.random() < 0.02) {
                TransactionResponse errorResponse = new TransactionResponse();
                errorResponse.setTransactionId(null);
                errorResponse.setAccountId(loyaltyAccountId);
                errorResponse.setPoints(points);
                errorResponse.setReference(reference);
                errorResponse.setStatus("FAILED");
                errorResponse.setErrorCode("SYSTEM_ERROR");
                errorResponse.setErrorMessage("Credit processing temporarily unavailable");
                errorResponse.setProcessedAt(LocalDateTime.now());
                
                logger.warn("MOCK: Simulated credit error for account: {}", loyaltyAccountId);
                return errorResponse;
            }
            
            // Process successful credit
            Integer newBalance = currentBalance + points;
            accountBalances.put(loyaltyAccountId, newBalance);
            
            // Simulate processing time
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            TransactionResponse response = new TransactionResponse();
            response.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8));
            response.setAccountId(loyaltyAccountId);
            response.setPoints(points);
            response.setReference(reference);
            response.setStatus("SUCCESS");
            response.setBalanceBefore(currentBalance);
            response.setBalanceAfter(newBalance);
            response.setProcessedAt(LocalDateTime.now());
            
            logger.info("MOCK: Successfully credited {} points to {}, new balance: {}", 
                    points, loyaltyAccountId, newBalance);
            
            return response;
        });
    }

    @Override
    public Mono<Boolean> isHealthy() {
        logger.debug("MOCK: Loyalty API health check - always healthy");
        return Mono.just(true);
    }

    // Additional methods for testing
    public void setAccountBalance(String loyaltyAccountId, Integer balance) {
        accountBalances.put(loyaltyAccountId, balance);
        logger.info("MOCK: Set balance for account {} to {} points", loyaltyAccountId, balance);
    }

    public void setAccountTier(String loyaltyAccountId, String tier) {
        accountTiers.put(loyaltyAccountId, tier);
        logger.info("MOCK: Set tier for account {} to {}", loyaltyAccountId, tier);
    }

    public Integer getCurrentBalance(String loyaltyAccountId) {
        return accountBalances.get(loyaltyAccountId);
    }

    public void resetMockData() {
        logger.info("MOCK: Resetting all account data");
        initializeMockData();
    }
}