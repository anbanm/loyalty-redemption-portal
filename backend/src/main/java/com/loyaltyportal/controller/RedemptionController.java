package com.loyaltyportal.controller;

import com.loyaltyportal.dto.BalanceResponse;
import com.loyaltyportal.dto.CreateOrderRequest;
import com.loyaltyportal.dto.OrderSummaryDto;
import com.loyaltyportal.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/redemption")
@Tag(name = "Redemption", description = "Loyalty points redemption operations")
public class RedemptionController {

    private static final Logger logger = LoggerFactory.getLogger(RedemptionController.class);

    private final RedemptionService redemptionService;

    @Autowired
    public RedemptionController(RedemptionService redemptionService) {
        this.redemptionService = redemptionService;
    }

    @GetMapping("/balance/{companyId}")
    @Operation(summary = "Check loyalty points balance", 
               description = "Retrieve the current loyalty points balance for a company")
    public Mono<ResponseEntity<BalanceResponse>> checkBalance(
            @Parameter(description = "Company ID") @PathVariable UUID companyId) {
        
        logger.info("Checking balance for company: {}", companyId);
        
        return redemptionService.checkBalance(companyId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> logger.info("Balance check successful for company: {}", companyId))
                .doOnError(error -> logger.error("Balance check failed for company {}: {}", 
                        companyId, error.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/orders")
    @Operation(summary = "Create redemption order", 
               description = "Create a new loyalty points redemption order")
    public Mono<ResponseEntity<OrderSummaryDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        logger.info("Creating order for company: {} by account manager: {}", 
                request.getCompanyId(), request.getAccountManagerId());
        
        return redemptionService.createOrder(request)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> logger.info("Order created successfully"))
                .doOnError(error -> logger.error("Order creation failed: {}", error.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/orders/{orderId}/process")
    @Operation(summary = "Process redemption order", 
               description = "Process an existing order (debit points and initiate fulfillment)")
    public Mono<ResponseEntity<OrderSummaryDto>> processOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        
        logger.info("Processing order: {}", orderId);
        
        return redemptionService.processOrder(orderId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> logger.info("Order processed successfully: {}", orderId))
                .doOnError(error -> logger.error("Order processing failed for {}: {}", 
                        orderId, error.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/orders/{orderId}/cancel")
    @Operation(summary = "Cancel redemption order", 
               description = "Cancel an existing order and refund points if necessary")
    public Mono<ResponseEntity<OrderSummaryDto>> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {
        
        logger.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        String cancellationReason = reason != null ? reason : "Cancelled by user";
        
        return redemptionService.cancelOrder(orderId, cancellationReason)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> logger.info("Order cancelled successfully: {}", orderId))
                .doOnError(error -> logger.error("Order cancellation failed for {}: {}", 
                        orderId, error.getMessage()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}