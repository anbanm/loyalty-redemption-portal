package com.loyaltyportal.controller;

import com.loyaltyportal.service.mock.MockLoyaltyApiClient;
import com.loyaltyportal.service.mock.MockNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MockController.class)
@ActiveProfiles("test")
public class MockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MockLoyaltyApiClient mockLoyaltyApiClient;

    @MockBean
    private MockNotificationService mockNotificationService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MockLoyaltyApiClient mockLoyaltyApiClient() {
            return null; // Will be mocked
        }
        
        @Bean
        public MockNotificationService mockNotificationService() {
            return null; // Will be mocked
        }
    }

    @Test
    void testSetMockBalance() throws Exception {
        mockMvc.perform(post("/mock/loyalty/balance/TEST001")
                .param("balance", "75000"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance set successfully"));
    }

    @Test
    void testGetMockBalance() throws Exception {
        when(mockLoyaltyApiClient.getCurrentBalance("TEST001")).thenReturn(75000);

        mockMvc.perform(get("/mock/loyalty/balance/TEST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("TEST001"))
                .andExpect(jsonPath("$.balance").value(75000));
    }

    @Test
    void testSetMockTier() throws Exception {
        mockMvc.perform(post("/mock/loyalty/tier/TEST001")
                .param("tier", "PLATINUM"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tier set successfully"));
    }

    @Test
    void testResetLoyaltyMockData() throws Exception {
        mockMvc.perform(post("/mock/loyalty/reset"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loyalty mock data reset successfully"));
    }

    @Test
    void testGetSentEmails() throws Exception {
        when(mockNotificationService.getAllSentEmails()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/mock/notifications/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetNotificationStatistics() throws Exception {
        Map<String, Long> emailsByType = new HashMap<>();
        emailsByType.put("ORDER_CONFIRMATION", 5L);
        emailsByType.put("SHIPPING_NOTIFICATION", 3L);

        MockNotificationService.NotificationStatistics stats = 
                new MockNotificationService.NotificationStatistics(8L, emailsByType);

        when(mockNotificationService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/mock/notifications/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmails").value(8))
                .andExpect(jsonPath("$.emailsByType.ORDER_CONFIRMATION").value(5))
                .andExpect(jsonPath("$.emailsByType.SHIPPING_NOTIFICATION").value(3));
    }

    @Test
    void testClearEmailHistory() throws Exception {
        mockMvc.perform(post("/mock/notifications/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email history cleared successfully"));
    }

    @Test
    void testGetMockServicesStatus() throws Exception {
        mockMvc.perform(get("/mock/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loyaltyApiMockEnabled").value(true))
                .andExpect(jsonPath("$.notificationMockEnabled").value(true))
                .andExpect(jsonPath("$.virtualFulfillmentMockEnabled").value(false));
    }

    @Test
    void testResetAllMockData() throws Exception {
        mockMvc.perform(post("/mock/reset-all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reset 2 mock services successfully"));
    }
}