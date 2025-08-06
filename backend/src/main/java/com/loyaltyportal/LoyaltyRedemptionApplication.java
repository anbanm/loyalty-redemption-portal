package com.loyaltyportal;

import org.broadleafcommerce.common.config.EnableBroadleafSiteAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBroadleafSiteAutoConfiguration
public class LoyaltyRedemptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyRedemptionApplication.class, args);
    }
}