package com.loyaltyportal.service;

import com.loyaltyportal.dto.BalanceResponse;
import com.loyaltyportal.dto.TransactionRequest;
import com.loyaltyportal.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class LoyaltyApiClient {

    private static final Logger logger = LoggerFactory.getLogger(LoyaltyApiClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final Duration timeout;
    private final int maxRetryAttempts;
    private final Duration retryDelay;

    public LoyaltyApiClient(
            @Value("${loyalty.api.base-url}") String baseUrl,
            @Value("${loyalty.api.api-key}") String apiKey,
            @Value("${loyalty.api.timeout:30s}") Duration timeout,
            @Value("${loyalty.api.retry.max-attempts:3}") int maxRetryAttempts,
            @Value("${loyalty.api.retry.backoff-delay:1s}") Duration retryDelay) {
        
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelay = retryDelay;
        
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    /**
     * Check the loyalty points balance for a company account
     */
    public Mono<BalanceResponse> getBalance(String loyaltyAccountId) {
        logger.info("Fetching balance for loyalty account: {}", loyaltyAccountId);
        
        return webClient.get()
                .uri("/balance/{accountId}", loyaltyAccountId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Client error: " + body)))
                .onStatus(HttpStatus::is5xxServerError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Server error: " + body)))
                .bodyToMono(BalanceResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryDelay)
                        .filter(throwable -> !(throwable instanceof WebClientResponseException) ||
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> logger.info("Successfully fetched balance for account {}: {} points", 
                        loyaltyAccountId, response.getBalance()))
                .doOnError(error -> logger.error("Failed to fetch balance for account {}: {}", 
                        loyaltyAccountId, error.getMessage()));
    }

    /**
     * Debit points from a loyalty account
     */
    public Mono<TransactionResponse> debitPoints(String loyaltyAccountId, Integer points, String reference) {
        logger.info("Debiting {} points from loyalty account: {} with reference: {}", 
                points, loyaltyAccountId, reference);
        
        TransactionRequest request = new TransactionRequest(loyaltyAccountId, points, reference);
        
        return webClient.post()
                .uri("/debit")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Debit failed: " + body)))
                .onStatus(HttpStatus::is5xxServerError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Server error during debit: " + body)))
                .bodyToMono(TransactionResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryDelay)
                        .filter(throwable -> !(throwable instanceof WebClientResponseException) ||
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> logger.info("Successfully debited {} points from account {}, transaction ID: {}", 
                        points, loyaltyAccountId, response.getTransactionId()))
                .doOnError(error -> logger.error("Failed to debit points from account {}: {}", 
                        loyaltyAccountId, error.getMessage()));
    }

    /**
     * Credit/refund points to a loyalty account
     */
    public Mono<TransactionResponse> creditPoints(String loyaltyAccountId, Integer points, String reference) {
        logger.info("Crediting {} points to loyalty account: {} with reference: {}", 
                points, loyaltyAccountId, reference);
        
        TransactionRequest request = new TransactionRequest(loyaltyAccountId, points, reference);
        
        return webClient.post()
                .uri("/credit")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Credit failed: " + body)))
                .onStatus(HttpStatus::is5xxServerError, response -> 
                    response.bodyToMono(String.class)
                            .map(body -> new LoyaltyApiException("Server error during credit: " + body)))
                .bodyToMono(TransactionResponse.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(maxRetryAttempts, retryDelay)
                        .filter(throwable -> !(throwable instanceof WebClientResponseException) ||
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> logger.info("Successfully credited {} points to account {}, transaction ID: {}", 
                        points, loyaltyAccountId, response.getTransactionId()))
                .doOnError(error -> logger.error("Failed to credit points to account {}: {}", 
                        loyaltyAccountId, error.getMessage()));
    }

    /**
     * Check if the loyalty API is available
     */
    public Mono<Boolean> isHealthy() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> true)
                .onErrorReturn(false)
                .doOnSuccess(healthy -> logger.debug("Loyalty API health check: {}", healthy ? "OK" : "FAILED"));
    }

    public static class LoyaltyApiException extends RuntimeException {
        public LoyaltyApiException(String message) {
            super(message);
        }
        
        public LoyaltyApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}