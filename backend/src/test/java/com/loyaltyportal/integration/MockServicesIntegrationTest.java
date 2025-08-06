package com.loyaltyportal.integration;

import com.loyaltyportal.dto.BalanceResponse;
import com.loyaltyportal.dto.CreateOrderRequest;
import com.loyaltyportal.dto.OrderSummaryDto;
import com.loyaltyportal.entity.*;
import com.loyaltyportal.repository.*;
import com.loyaltyportal.service.RedemptionService;
import com.loyaltyportal.service.mock.MockLoyaltyApiClient;
import com.loyaltyportal.service.mock.MockNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MockServicesIntegrationTest {

    @Autowired
    private RedemptionService redemptionService;

    @Autowired
    private MockLoyaltyApiClient mockLoyaltyApiClient;

    @Autowired
    private MockNotificationService mockNotificationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private AccountManagerRepository accountManagerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Company testCompany;
    private AccountManager testAccountManager;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clear mock data
        mockLoyaltyApiClient.resetMockData();
        mockNotificationService.clearEmailHistory();

        // Create test data
        testCompany = new Company("Test Company", "TEST001", "GOLD");
        testCompany = companyRepository.save(testCompany);

        testAccountManager = new AccountManager(testCompany, "test@test.com", "Test Manager");
        testAccountManager = accountManagerRepository.save(testAccountManager);

        testProduct = new Product("TEST-PRODUCT-001", "Test Product", ProductType.VIRTUAL, 1000);
        testProduct.setCategory("Test Category");
        testProduct = productRepository.save(testProduct);

        // Initialize inventory
        Inventory inventory = new Inventory(testProduct, 100);
        inventoryRepository.save(inventory);

        // Set up mock loyalty account with sufficient balance
        mockLoyaltyApiClient.setAccountBalance("TEST001", 50000);
    }

    @Test
    void testBalanceCheck_WithMockService() {
        // Test balance check with mock service
        StepVerifier.create(redemptionService.checkBalance(testCompany.getId()))
                .assertNext(balance -> {
                    assertThat(balance.getAccountId()).isEqualTo("TEST001");
                    assertThat(balance.getBalance()).isEqualTo(50000);
                    assertThat(balance.getTierLevel()).isEqualTo("GOLD");
                })
                .verifyComplete();
    }

    @Test
    void testFullOrderFlow_WithMockServices() {
        // Create order request
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                testProduct.getId(), 2);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testCompany.getId(), testAccountManager.getId(), Arrays.asList(itemRequest));

        // Test order creation
        StepVerifier.create(redemptionService.createOrder(orderRequest))
                .assertNext(orderSummary -> {
                    assertThat(orderSummary.getCompanyName()).isEqualTo("Test Company");
                    assertThat(orderSummary.getTotalPoints()).isEqualTo(2000); // 2 items * 1000 points
                    assertThat(orderSummary.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(orderSummary.getItemCount()).isEqualTo(1);
                })
                .verifyComplete();

        // Verify no emails sent yet (order not processed)
        assertThat(mockNotificationService.getEmailCount()).isEqualTo(0);
    }

    @Test
    void testOrderProcessing_WithMockServices() {
        // Create and save an order first
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                testProduct.getId(), 1);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testCompany.getId(), testAccountManager.getId(), Arrays.asList(itemRequest));

        OrderSummaryDto createdOrder = redemptionService.createOrder(orderRequest).block();
        assertThat(createdOrder).isNotNull();

        // Process the order
        StepVerifier.create(redemptionService.processOrder(createdOrder.getId()))
                .assertNext(processedOrder -> {
                    assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
                })
                .expectComplete()
                .verify(Duration.ofSeconds(10));

        // Verify emails were sent
        assertThat(mockNotificationService.getEmailCount()).isGreaterThan(0);
        
        List<MockNotificationService.MockEmailRecord> emails = 
                mockNotificationService.getEmailsByRecipient("test@test.com");
        assertThat(emails).hasSize(1);
        assertThat(emails.get(0).getType()).isEqualTo("ORDER_CONFIRMATION");

        // Verify loyalty points were debited
        Integer newBalance = mockLoyaltyApiClient.getCurrentBalance("TEST001");
        assertThat(newBalance).isEqualTo(49000); // 50000 - 1000
    }

    @Test
    void testInsufficientBalance_WithMockService() {
        // Set low balance
        mockLoyaltyApiClient.setAccountBalance("TEST001", 500);

        // Try to create order for 1000 points
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                testProduct.getId(), 1);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testCompany.getId(), testAccountManager.getId(), Arrays.asList(itemRequest));

        OrderSummaryDto createdOrder = redemptionService.createOrder(orderRequest).block();
        assertThat(createdOrder).isNotNull();

        // Process the order - should fail due to insufficient balance
        StepVerifier.create(redemptionService.processOrder(createdOrder.getId()))
                .assertNext(processedOrder -> {
                    assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
                })
                .verifyComplete();

        // Balance should remain unchanged
        Integer balance = mockLoyaltyApiClient.getCurrentBalance("TEST001");
        assertThat(balance).isEqualTo(500);
    }

    @Test
    void testOrderCancellation_WithMockServices() {
        // Create and process an order first
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                testProduct.getId(), 1);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testCompany.getId(), testAccountManager.getId(), Arrays.asList(itemRequest));

        OrderSummaryDto createdOrder = redemptionService.createOrder(orderRequest).block();
        OrderSummaryDto processedOrder = redemptionService.processOrder(createdOrder.getId()).block();
        
        assertThat(processedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);

        // Cancel the order
        StepVerifier.create(redemptionService.cancelOrder(processedOrder.getId(), "Test cancellation"))
                .assertNext(cancelledOrder -> {
                    assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                })
                .verifyComplete();

        // Verify points were refunded
        Integer finalBalance = mockLoyaltyApiClient.getCurrentBalance("TEST001");
        assertThat(finalBalance).isEqualTo(50000); // Should be back to original
    }

    @Test
    void testMockLoyaltyApiClient_DirectUsage() {
        // Test direct usage of mock client
        StepVerifier.create(mockLoyaltyApiClient.getBalance("TEST001"))
                .assertNext(balance -> {
                    assertThat(balance.getAccountId()).isEqualTo("TEST001");
                    assertThat(balance.getBalance()).isEqualTo(50000);
                })
                .verifyComplete();

        // Test debit
        StepVerifier.create(mockLoyaltyApiClient.debitPoints("TEST001", 5000, "TEST-DEBIT"))
                .assertNext(transaction -> {
                    assertThat(transaction.isSuccessful()).isTrue();
                    assertThat(transaction.getBalanceAfter()).isEqualTo(45000);
                })
                .verifyComplete();

        // Test credit/refund
        StepVerifier.create(mockLoyaltyApiClient.creditPoints("TEST001", 2000, "TEST-REFUND"))
                .assertNext(transaction -> {
                    assertThat(transaction.isSuccessful()).isTrue();
                    assertThat(transaction.getBalanceAfter()).isEqualTo(47000);
                })
                .verifyComplete();
    }

    @Test
    void testNotificationMockService() {
        // Clear any existing emails
        mockNotificationService.clearEmailHistory();
        assertThat(mockNotificationService.getEmailCount()).isEqualTo(0);

        // Create a test order to generate notifications
        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest(
                testProduct.getId(), 1);
        CreateOrderRequest orderRequest = new CreateOrderRequest(
                testCompany.getId(), testAccountManager.getId(), Arrays.asList(itemRequest));

        OrderSummaryDto createdOrder = redemptionService.createOrder(orderRequest).block();
        redemptionService.processOrder(createdOrder.getId()).block();

        // Verify notifications were recorded
        assertThat(mockNotificationService.getEmailCount()).isGreaterThan(0);

        List<MockNotificationService.MockEmailRecord> customerEmails = 
                mockNotificationService.getEmailsByRecipient("test@test.com");
        assertThat(customerEmails).isNotEmpty();

        MockNotificationService.NotificationStatistics stats = mockNotificationService.getStatistics();
        assertThat(stats.getTotalEmails()).isGreaterThan(0);
    }
}