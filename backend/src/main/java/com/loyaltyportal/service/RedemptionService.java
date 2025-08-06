package com.loyaltyportal.service;

import com.loyaltyportal.dto.BalanceResponse;
import com.loyaltyportal.dto.CreateOrderRequest;
import com.loyaltyportal.dto.OrderSummaryDto;
import com.loyaltyportal.dto.TransactionResponse;
import com.loyaltyportal.entity.*;
import com.loyaltyportal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RedemptionService {

    private static final Logger logger = LoggerFactory.getLogger(RedemptionService.class);

    private final RedemptionOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CompanyRepository companyRepository;
    private final AccountManagerRepository accountManagerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final LoyaltyApiClient loyaltyApiClient;
    private final LoyaltyTransactionService transactionService;
    private final OrderWorkflowService workflowService;

    @Autowired
    public RedemptionService(
            RedemptionOrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CompanyRepository companyRepository,
            AccountManagerRepository accountManagerRepository,
            ProductRepository productRepository,
            InventoryService inventoryService,
            LoyaltyApiClient loyaltyApiClient,
            LoyaltyTransactionService transactionService,
            OrderWorkflowService workflowService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.companyRepository = companyRepository;
        this.accountManagerRepository = accountManagerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.loyaltyApiClient = loyaltyApiClient;
        this.transactionService = transactionService;
        this.workflowService = workflowService;
    }

    /**
     * Check loyalty points balance for a company
     */
    public Mono<BalanceResponse> checkBalance(UUID companyId) {
        logger.info("Checking balance for company: {}", companyId);
        
        return Mono.fromCallable(() -> companyRepository.findById(companyId))
                .flatMap(companyOpt -> {
                    if (companyOpt.isEmpty()) {
                        return Mono.error(new RedemptionException("Company not found: " + companyId));
                    }
                    
                    Company company = companyOpt.get();
                    if (company.getLoyaltyAccountId() == null) {
                        return Mono.error(new RedemptionException("Company has no loyalty account configured"));
                    }
                    
                    return loyaltyApiClient.getBalance(company.getLoyaltyAccountId());
                });
    }

    /**
     * Create a new redemption order
     */
    public Mono<OrderSummaryDto> createOrder(CreateOrderRequest request) {
        logger.info("Creating redemption order for company: {} by account manager: {}", 
                request.getCompanyId(), request.getAccountManagerId());
        
        return Mono.fromCallable(() -> validateAndPrepareOrder(request))
                .flatMap(orderData -> processOrderCreation(orderData))
                .doOnSuccess(order -> logger.info("Successfully created order: {}", order.getOrderNumber()))
                .doOnError(error -> logger.error("Failed to create order: {}", error.getMessage()));
    }

    /**
     * Process an existing order (attempt to debit points and fulfill items)
     */
    public Mono<OrderSummaryDto> processOrder(UUID orderId) {
        logger.info("Processing redemption order: {}", orderId);
        
        return Mono.fromCallable(() -> getOrderForProcessing(orderId))
                .flatMap(this::executeOrderProcessing)
                .doOnSuccess(order -> logger.info("Successfully processed order: {}", order.getOrderNumber()))
                .doOnError(error -> logger.error("Failed to process order {}: {}", orderId, error.getMessage()));
    }

    /**
     * Cancel an order and refund points if necessary
     */
    public Mono<OrderSummaryDto> cancelOrder(UUID orderId, String reason) {
        logger.info("Cancelling redemption order: {} with reason: {}", orderId, reason);
        
        return Mono.fromCallable(() -> getOrderForCancellation(orderId))
                .flatMap(order -> executeCancellation(order, reason))
                .doOnSuccess(order -> logger.info("Successfully cancelled order: {}", order.getOrderNumber()))
                .doOnError(error -> logger.error("Failed to cancel order {}: {}", orderId, error.getMessage()));
    }

    // Private helper methods
    private OrderCreationData validateAndPrepareOrder(CreateOrderRequest request) {
        // Validate company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RedemptionException("Company not found"));
        
        if (!company.getIsActive()) {
            throw new RedemptionException("Company is not active");
        }
        
        if (company.getLoyaltyAccountId() == null) {
            throw new RedemptionException("Company has no loyalty account configured");
        }

        // Validate account manager
        AccountManager accountManager = accountManagerRepository.findById(request.getAccountManagerId())
                .orElseThrow(() -> new RedemptionException("Account manager not found"));
        
        if (!accountManager.getIsActive()) {
            throw new RedemptionException("Account manager is not active");
        }
        
        if (!accountManager.getCompany().getId().equals(company.getId())) {
            throw new RedemptionException("Account manager does not belong to this company");
        }

        // Validate and calculate order items
        List<OrderItemData> itemsData = new ArrayList<>();
        int totalPoints = 0;
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RedemptionException("Product not found: " + itemRequest.getProductId()));
            
            if (!product.getIsActive()) {
                throw new RedemptionException("Product is not active: " + product.getSku());
            }
            
            // Check inventory for physical products
            if (ProductType.PHYSICAL.equals(product.getProductType())) {
                if (!inventoryService.checkAvailability(product.getId(), itemRequest.getQuantity())) {
                    throw new RedemptionException("Insufficient inventory for product: " + product.getSku());
                }
            }
            
            int itemTotal = product.getPointsCost() * itemRequest.getQuantity();
            totalPoints += itemTotal;
            
            itemsData.add(new OrderItemData(product, itemRequest.getQuantity(), product.getPointsCost()));
        }

        return new OrderCreationData(company, accountManager, itemsData, totalPoints, 
                request.getShippingAddress(), request.getSpecialInstructions());
    }

    private Mono<OrderSummaryDto> processOrderCreation(OrderCreationData orderData) {
        return Mono.fromCallable(() -> {
            // Create the order
            RedemptionOrder order = new RedemptionOrder(
                    orderData.company, 
                    orderData.accountManager, 
                    orderData.totalPoints
            );
            order.setShippingAddress(orderData.shippingAddress);
            order.setSpecialInstructions(orderData.specialInstructions);
            order = orderRepository.save(order);

            // Create order items and reserve inventory
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemData itemData : orderData.itemsData) {
                OrderItem orderItem = new OrderItem(order, itemData.product, itemData.quantity, itemData.pointsPerItem);
                orderItems.add(orderItemRepository.save(orderItem));
                
                // Reserve inventory for physical products
                if (ProductType.PHYSICAL.equals(itemData.product.getProductType())) {
                    inventoryService.reserveInventory(itemData.product.getId(), itemData.quantity);
                }
            }

            order.setItems(orderItems);
            return order;
        })
        .map(this::convertToOrderSummary);
    }

    private RedemptionOrder getOrderForProcessing(UUID orderId) {
        RedemptionOrder order = orderRepository.findByIdWithItemsAndTransactions(orderId)
                .orElseThrow(() -> new RedemptionException("Order not found"));
        
        if (!order.isPending()) {
            throw new RedemptionException("Order is not in pending status");
        }
        
        return order;
    }

    private Mono<OrderSummaryDto> executeOrderProcessing(RedemptionOrder order) {
        // First, try to debit points
        return loyaltyApiClient.debitPoints(
                order.getCompany().getLoyaltyAccountId(),
                order.getTotalPoints(),
                "ORDER-" + order.getOrderNumber()
        )
        .flatMap(transactionResponse -> {
            if (transactionResponse.isSuccessful()) {
                return Mono.fromCallable(() -> completeOrderProcessing(order, transactionResponse));
            } else {
                return Mono.fromCallable(() -> failOrderProcessing(order, transactionResponse.getErrorMessage()));
            }
        })
        .onErrorResume(error -> {
            logger.error("Points debit failed for order {}: {}", order.getOrderNumber(), error.getMessage());
            return Mono.fromCallable(() -> failOrderProcessing(order, error.getMessage()));
        });
    }

    private OrderSummaryDto completeOrderProcessing(RedemptionOrder order, TransactionResponse transactionResponse) {
        // Record the successful transaction
        transactionService.recordTransaction(
                order, 
                TransactionType.DEBIT, 
                order.getTotalPoints(),
                transactionResponse.getTransactionId(),
                TransactionStatus.COMPLETED
        );

        // Mark order as processing
        order.markAsProcessing();
        order = orderRepository.save(order);

        // Initiate fulfillment workflows
        workflowService.initiateOrderFulfillment(order);

        return convertToOrderSummary(order);
    }

    private OrderSummaryDto failOrderProcessing(RedemptionOrder order, String errorMessage) {
        // Record the failed transaction
        transactionService.recordFailedTransaction(
                order, 
                TransactionType.DEBIT, 
                order.getTotalPoints(),
                errorMessage
        );

        // Release any reserved inventory
        for (OrderItem item : order.getItems()) {
            if (ProductType.PHYSICAL.equals(item.getProduct().getProductType())) {
                inventoryService.releaseReservation(item.getProduct().getId(), item.getQuantity());
            }
        }

        // Mark order as failed
        order.setStatus(OrderStatus.FAILED);
        order = orderRepository.save(order);

        return convertToOrderSummary(order);
    }

    private RedemptionOrder getOrderForCancellation(UUID orderId) {
        RedemptionOrder order = orderRepository.findByIdWithItemsAndTransactions(orderId)
                .orElseThrow(() -> new RedemptionException("Order not found"));
        
        if (order.isCompleted()) {
            throw new RedemptionException("Cannot cancel a completed order");
        }
        
        return order;
    }

    private Mono<OrderSummaryDto> executeCancellation(RedemptionOrder order, String reason) {
        // If order was processed, refund the points
        if (order.isProcessing()) {
            return loyaltyApiClient.creditPoints(
                    order.getCompany().getLoyaltyAccountId(),
                    order.getTotalPoints(),
                    "REFUND-" + order.getOrderNumber()
            )
            .flatMap(refundResponse -> {
                if (refundResponse.isSuccessful()) {
                    return Mono.fromCallable(() -> completeCancellation(order, reason, refundResponse));
                } else {
                    return Mono.error(new RedemptionException("Failed to refund points: " + refundResponse.getErrorMessage()));
                }
            });
        } else {
            return Mono.fromCallable(() -> completeCancellation(order, reason, null));
        }
    }

    private OrderSummaryDto completeCancellation(RedemptionOrder order, String reason, TransactionResponse refundResponse) {
        // Record refund transaction if applicable
        if (refundResponse != null && refundResponse.isSuccessful()) {
            transactionService.recordTransaction(
                    order,
                    TransactionType.REFUND,
                    order.getTotalPoints(),
                    refundResponse.getTransactionId(),
                    TransactionStatus.COMPLETED
            );
        }

        // Release reserved inventory
        for (OrderItem item : order.getItems()) {
            if (ProductType.PHYSICAL.equals(item.getProduct().getProductType())) {
                inventoryService.releaseReservation(item.getProduct().getId(), item.getQuantity());
            }
        }

        // Mark order as cancelled
        order.markAsCancelled(reason);
        order = orderRepository.save(order);

        return convertToOrderSummary(order);
    }

    private OrderSummaryDto convertToOrderSummary(RedemptionOrder order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getCompany().getName(),
                order.getAccountManager().getName(),
                order.getTotalPoints(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCompletedAt(),
                order.getItems().size()
        );
    }

    // Helper classes
    private static class OrderCreationData {
        final Company company;
        final AccountManager accountManager;
        final List<OrderItemData> itemsData;
        final int totalPoints;
        final String shippingAddress;
        final String specialInstructions;

        OrderCreationData(Company company, AccountManager accountManager, List<OrderItemData> itemsData, 
                         int totalPoints, String shippingAddress, String specialInstructions) {
            this.company = company;
            this.accountManager = accountManager;
            this.itemsData = itemsData;
            this.totalPoints = totalPoints;
            this.shippingAddress = shippingAddress;
            this.specialInstructions = specialInstructions;
        }
    }

    private static class OrderItemData {
        final Product product;
        final int quantity;
        final int pointsPerItem;

        OrderItemData(Product product, int quantity, int pointsPerItem) {
            this.product = product;
            this.quantity = quantity;
            this.pointsPerItem = pointsPerItem;
        }
    }

    public static class RedemptionException extends RuntimeException {
        public RedemptionException(String message) {
            super(message);
        }
        
        public RedemptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}