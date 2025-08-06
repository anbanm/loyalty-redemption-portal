package com.loyaltyportal.controller;

import com.loyaltyportal.service.mock.MockLoyaltyApiClient;
import com.loyaltyportal.service.mock.MockNotificationService;
import com.loyaltyportal.service.mock.MockVirtualFulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mock")
@ConditionalOnProperty(name = "loyalty.mock.enabled", havingValue = "true")
@Tag(name = "Mock Services", description = "Mock service management for testing")
public class MockController {

    private static final Logger logger = LoggerFactory.getLogger(MockController.class);

    private final MockLoyaltyApiClient mockLoyaltyApiClient;
    private final MockNotificationService mockNotificationService;
    private final MockVirtualFulfillmentService mockVirtualFulfillmentService;

    @Autowired
    public MockController(
            @Autowired(required = false) MockLoyaltyApiClient mockLoyaltyApiClient,
            @Autowired(required = false) MockNotificationService mockNotificationService,
            @Autowired(required = false) MockVirtualFulfillmentService mockVirtualFulfillmentService) {
        this.mockLoyaltyApiClient = mockLoyaltyApiClient;
        this.mockNotificationService = mockNotificationService;
        this.mockVirtualFulfillmentService = mockVirtualFulfillmentService;
    }

    // Loyalty API Mock Management
    @PostMapping("/loyalty/balance/{accountId}")
    @Operation(summary = "Set mock balance", description = "Set the balance for a mock loyalty account")
    public ResponseEntity<String> setMockBalance(
            @Parameter(description = "Loyalty Account ID") @PathVariable String accountId,
            @Parameter(description = "Balance amount") @RequestParam Integer balance) {
        
        if (mockLoyaltyApiClient == null) {
            return ResponseEntity.badRequest().body("Mock loyalty API client not available");
        }
        
        mockLoyaltyApiClient.setAccountBalance(accountId, balance);
        logger.info("Set mock balance for account {} to {} points", accountId, balance);
        return ResponseEntity.ok("Balance set successfully");
    }

    @PostMapping("/loyalty/tier/{accountId}")
    @Operation(summary = "Set mock tier", description = "Set the tier level for a mock loyalty account")
    public ResponseEntity<String> setMockTier(
            @Parameter(description = "Loyalty Account ID") @PathVariable String accountId,
            @Parameter(description = "Tier level") @RequestParam String tier) {
        
        if (mockLoyaltyApiClient == null) {
            return ResponseEntity.badRequest().body("Mock loyalty API client not available");
        }
        
        mockLoyaltyApiClient.setAccountTier(accountId, tier);
        logger.info("Set mock tier for account {} to {}", accountId, tier);
        return ResponseEntity.ok("Tier set successfully");
    }

    @GetMapping("/loyalty/balance/{accountId}")
    @Operation(summary = "Get mock balance", description = "Get the current mock balance for an account")
    public ResponseEntity<Map<String, Object>> getMockBalance(
            @Parameter(description = "Loyalty Account ID") @PathVariable String accountId) {
        
        if (mockLoyaltyApiClient == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Integer balance = mockLoyaltyApiClient.getCurrentBalance(accountId);
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", accountId);
        response.put("balance", balance);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/loyalty/reset")
    @Operation(summary = "Reset loyalty mock data", description = "Reset all mock loyalty account data to defaults")
    public ResponseEntity<String> resetLoyaltyMockData() {
        if (mockLoyaltyApiClient == null) {
            return ResponseEntity.badRequest().body("Mock loyalty API client not available");
        }
        
        mockLoyaltyApiClient.resetMockData();
        logger.info("Reset mock loyalty API data");
        return ResponseEntity.ok("Loyalty mock data reset successfully");
    }

    // Notification Mock Management
    @GetMapping("/notifications/emails")
    @Operation(summary = "Get sent emails", description = "Retrieve all mock emails that have been sent")
    public ResponseEntity<List<MockNotificationService.MockEmailRecord>> getSentEmails() {
        if (mockNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<MockNotificationService.MockEmailRecord> emails = mockNotificationService.getAllSentEmails();
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/notifications/emails/recipient/{email}")
    @Operation(summary = "Get emails by recipient", description = "Get all emails sent to a specific recipient")
    public ResponseEntity<List<MockNotificationService.MockEmailRecord>> getEmailsByRecipient(
            @Parameter(description = "Recipient email address") @PathVariable String email) {
        
        if (mockNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<MockNotificationService.MockEmailRecord> emails = mockNotificationService.getEmailsByRecipient(email);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/notifications/emails/type/{type}")
    @Operation(summary = "Get emails by type", description = "Get all emails of a specific type")
    public ResponseEntity<List<MockNotificationService.MockEmailRecord>> getEmailsByType(
            @Parameter(description = "Email type") @PathVariable String type) {
        
        if (mockNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<MockNotificationService.MockEmailRecord> emails = mockNotificationService.getEmailsByType(type);
        return ResponseEntity.ok(emails);
    }

    @GetMapping("/notifications/statistics")
    @Operation(summary = "Get notification statistics", description = "Get email sending statistics")
    public ResponseEntity<MockNotificationService.NotificationStatistics> getNotificationStatistics() {
        if (mockNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        MockNotificationService.NotificationStatistics stats = mockNotificationService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/notifications/clear")
    @Operation(summary = "Clear email history", description = "Clear all mock email history")
    public ResponseEntity<String> clearEmailHistory() {
        if (mockNotificationService == null) {
            return ResponseEntity.badRequest().body("Mock notification service not available");
        }
        
        mockNotificationService.clearEmailHistory();
        logger.info("Cleared mock email history");
        return ResponseEntity.ok("Email history cleared successfully");
    }

    // Virtual Fulfillment Mock Management
    @GetMapping("/fulfillment/virtual/status/{fulfillmentId}")
    @Operation(summary = "Get fulfillment status", description = "Get the status of a virtual fulfillment")
    public ResponseEntity<MockVirtualFulfillmentService.VirtualFulfillmentRecord> getFulfillmentStatus(
            @Parameter(description = "Fulfillment ID") @PathVariable String fulfillmentId) {
        
        if (mockVirtualFulfillmentService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        MockVirtualFulfillmentService.VirtualFulfillmentRecord record = 
                mockVirtualFulfillmentService.getFulfillmentStatus(fulfillmentId);
        
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(record);
    }

    @GetMapping("/fulfillment/virtual/customer/{email}")
    @Operation(summary = "Get fulfillments by customer", description = "Get all fulfillments for a customer")
    public ResponseEntity<Map<String, MockVirtualFulfillmentService.VirtualFulfillmentRecord>> getFulfillmentsByCustomer(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        if (mockVirtualFulfillmentService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, MockVirtualFulfillmentService.VirtualFulfillmentRecord> fulfillments = 
                mockVirtualFulfillmentService.getFulfillmentsByCustomer(email);
        
        return ResponseEntity.ok(fulfillments);
    }

    @GetMapping("/fulfillment/virtual/statistics")
    @Operation(summary = "Get fulfillment statistics", description = "Get virtual fulfillment statistics")
    public ResponseEntity<MockVirtualFulfillmentService.FulfillmentStatistics> getFulfillmentStatistics() {
        if (mockVirtualFulfillmentService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        MockVirtualFulfillmentService.FulfillmentStatistics stats = 
                mockVirtualFulfillmentService.getStatistics();
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/fulfillment/virtual/reset")
    @Operation(summary = "Reset fulfillment data", description = "Reset all virtual fulfillment mock data")
    public ResponseEntity<String> resetFulfillmentData() {
        if (mockVirtualFulfillmentService == null) {
            return ResponseEntity.badRequest().body("Mock virtual fulfillment service not available");
        }
        
        mockVirtualFulfillmentService.resetMockData();
        logger.info("Reset mock virtual fulfillment data");
        return ResponseEntity.ok("Virtual fulfillment data reset successfully");
    }

    // General Mock Management
    @GetMapping("/status")
    @Operation(summary = "Get mock services status", description = "Get the status of all mock services")
    public ResponseEntity<MockServicesStatus> getMockServicesStatus() {
        MockServicesStatus status = new MockServicesStatus(
                mockLoyaltyApiClient != null,
                mockNotificationService != null,
                mockVirtualFulfillmentService != null
        );
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/reset-all")
    @Operation(summary = "Reset all mock data", description = "Reset all mock services to their default state")
    public ResponseEntity<String> resetAllMockData() {
        int resetCount = 0;
        
        if (mockLoyaltyApiClient != null) {
            mockLoyaltyApiClient.resetMockData();
            resetCount++;
        }
        
        if (mockNotificationService != null) {
            mockNotificationService.clearEmailHistory();
            resetCount++;
        }
        
        if (mockVirtualFulfillmentService != null) {
            mockVirtualFulfillmentService.resetMockData();
            resetCount++;
        }
        
        logger.info("Reset {} mock services", resetCount);
        return ResponseEntity.ok(String.format("Reset %d mock services successfully", resetCount));
    }

    // DTO for mock services status
    public static class MockServicesStatus {
        private final boolean loyaltyApiMockEnabled;
        private final boolean notificationMockEnabled;
        private final boolean virtualFulfillmentMockEnabled;

        public MockServicesStatus(boolean loyaltyApiMockEnabled, boolean notificationMockEnabled, 
                                 boolean virtualFulfillmentMockEnabled) {
            this.loyaltyApiMockEnabled = loyaltyApiMockEnabled;
            this.notificationMockEnabled = notificationMockEnabled;
            this.virtualFulfillmentMockEnabled = virtualFulfillmentMockEnabled;
        }

        public boolean isLoyaltyApiMockEnabled() { return loyaltyApiMockEnabled; }
        public boolean isNotificationMockEnabled() { return notificationMockEnabled; }
        public boolean isVirtualFulfillmentMockEnabled() { return virtualFulfillmentMockEnabled; }
    }
}