package com.loyaltyportal.config;

import com.loyaltyportal.entity.*;
import com.loyaltyportal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final CompanyRepository companyRepository;
    private final AccountManagerRepository accountManagerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public DataInitializer(CompanyRepository companyRepository,
                          AccountManagerRepository accountManagerRepository,
                          ProductRepository productRepository,
                          InventoryRepository inventoryRepository) {
        this.companyRepository = companyRepository;
        this.accountManagerRepository = accountManagerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() == 0) {
            logger.info("Initializing sample data...");
            initializeSampleData();
            logger.info("Sample data initialization completed");
        } else {
            logger.info("Sample data already exists, skipping initialization");
        }
    }

    private void initializeSampleData() {
        // Create Companies
        Company acmeCorp = createCompany("ACME Corporation", "ACME-123456", "GOLD", 
                "123 Business Ave, Corporate City, CC 12345");
        Company techSolutions = createCompany("Tech Solutions Inc", "TECH-789012", "PLATINUM",
                "456 Innovation Blvd, Tech Valley, TV 67890");
        Company globalTrade = createCompany("Global Trade Partners", "GLOBAL-345678", "SILVER",
                "789 Commerce St, Trade Center, TC 13579");

        // Create Account Managers
        AccountManager johnDoe = createAccountManager("John", "Doe", "john.doe@acme.com", acmeCorp);
        AccountManager janeSmith = createAccountManager("Jane", "Smith", "jane.smith@techsolutions.com", techSolutions);
        AccountManager bobJohnson = createAccountManager("Bob", "Johnson", "bob.johnson@globaltrade.com", globalTrade);

        // Create Physical Products
        List<Product> physicalProducts = Arrays.asList(
            createProduct("Apple MacBook Pro 16\"", "Latest MacBook Pro with M3 chip, 16GB RAM, 512GB SSD", 
                         ProductType.PHYSICAL, 85000, "Electronics", "Apple", 
                         "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=300&fit=crop", true),
                         
            createProduct("Sony WH-1000XM5 Headphones", "Premium noise-canceling wireless headphones", 
                         ProductType.PHYSICAL, 35000, "Electronics", "Sony",
                         "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&h=300&fit=crop", true),
                         
            createProduct("Samsung 55\" QLED Smart TV", "4K QLED TV with HDR and smart features",
                         ProductType.PHYSICAL, 65000, "Electronics", "Samsung",
                         "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=400&h=300&fit=crop", true),
                         
            createProduct("Dyson V15 Vacuum Cleaner", "Cordless vacuum with laser dust detection",
                         ProductType.PHYSICAL, 45000, "Home & Garden", "Dyson",
                         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop", true),
                         
            createProduct("KitchenAid Stand Mixer", "Professional 6-quart stand mixer in multiple colors",
                         ProductType.PHYSICAL, 28000, "Home & Garden", "KitchenAid",
                         "https://images.unsplash.com/photo-1578662996442-48f60103fc96?w=400&h=300&fit=crop", true),
                         
            createProduct("Nike Air Jordan Sneakers", "Limited edition Air Jordan basketball shoes",
                         ProductType.PHYSICAL, 18000, "Fashion & Sports", "Nike",
                         "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=300&fit=crop", true),
                         
            createProduct("Yeti Rambler Tumbler", "Insulated stainless steel tumbler - 20oz",
                         ProductType.PHYSICAL, 3500, "Home & Garden", "Yeti",
                         "https://images.unsplash.com/photo-1544967882-82ad8943c9ec?w=400&h=300&fit=crop", true),
                         
            createProduct("Patagonia Down Jacket", "Lightweight down jacket for outdoor adventures",
                         ProductType.PHYSICAL, 22000, "Fashion & Sports", "Patagonia",
                         "https://images.unsplash.com/photo-1544966503-7e9eeaeb61ce?w=400&h=300&fit=crop", true),
                         
            createProduct("Instant Pot Duo 8-Quart", "Multi-use pressure cooker and slow cooker",
                         ProductType.PHYSICAL, 12000, "Home & Garden", "Instant Pot",
                         "https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=300&fit=crop", true),
                         
            createProduct("Fitbit Sense 2", "Advanced health and fitness smartwatch",
                         ProductType.PHYSICAL, 25000, "Electronics", "Fitbit",
                         "https://images.unsplash.com/photo-1544117519-31a4b719223d?w=400&h=300&fit=crop", true)
        );

        // Create Virtual Products
        List<Product> virtualProducts = Arrays.asList(
            createProduct("Netflix Premium Subscription", "12-month Netflix Premium subscription",
                         ProductType.VIRTUAL, 15000, "Entertainment", "Netflix",
                         "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=400&h=300&fit=crop", true),
                         
            createProduct("Spotify Premium Family", "6-month Spotify Premium Family plan",
                         ProductType.VIRTUAL, 8000, "Entertainment", "Spotify",
                         "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&h=300&fit=crop", true),
                         
            createProduct("Adobe Creative Cloud", "12-month Adobe Creative Cloud All Apps subscription",
                         ProductType.VIRTUAL, 55000, "Software", "Adobe",
                         "https://images.unsplash.com/photo-1611224923853-80b023f02d71?w=400&h=300&fit=crop", true),
                         
            createProduct("Microsoft Office 365", "Annual Microsoft Office 365 Business Premium",
                         ProductType.VIRTUAL, 32000, "Software", "Microsoft",
                         "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=300&fit=crop", true),
                         
            createProduct("Amazon Prime Membership", "Annual Amazon Prime membership with benefits",
                         ProductType.VIRTUAL, 12000, "Services", "Amazon",
                         "https://images.unsplash.com/photo-1523474253046-8cd2748b5fd2?w=400&h=300&fit=crop", true),
                         
            createProduct("LinkedIn Premium", "6-month LinkedIn Premium Career subscription",
                         ProductType.VIRTUAL, 18000, "Services", "LinkedIn",
                         "https://images.unsplash.com/photo-1611605698335-8b1569810432?w=400&h=300&fit=crop", true),
                         
            createProduct("Coursera Plus", "Annual Coursera Plus unlimited learning subscription",
                         ProductType.VIRTUAL, 35000, "Education", "Coursera",
                         "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=300&fit=crop", true),
                         
            createProduct("Masterclass All-Access", "Annual MasterClass All-Access Pass",
                         ProductType.VIRTUAL, 16000, "Education", "MasterClass",
                         "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=400&h=300&fit=crop", true),
                         
            createProduct("Zoom Pro", "12-month Zoom Pro video conferencing subscription",
                         ProductType.VIRTUAL, 14000, "Software", "Zoom",
                         "https://images.unsplash.com/photo-1588196749597-9ff075ee6b5b?w=400&h=300&fit=crop", true),
                         
            createProduct("Disney+ Bundle", "Annual Disney+ bundle with Hulu and ESPN+",
                         ProductType.VIRTUAL, 20000, "Entertainment", "Disney",
                         "https://images.unsplash.com/photo-1617897903246-719242758050?w=400&h=300&fit=crop", true)
        );

        // Save all products
        productRepository.saveAll(physicalProducts);
        productRepository.saveAll(virtualProducts);

        // Create inventory for physical products
        for (Product product : physicalProducts) {
            createInventoryEntry(product, getRandomStock(product.getPointsCost()));
        }

        // Virtual products have unlimited inventory
        for (Product product : virtualProducts) {
            createInventoryEntry(product, 999999);
        }

        logger.info("Created {} companies", companyRepository.count());
        logger.info("Created {} account managers", accountManagerRepository.count());
        logger.info("Created {} products", productRepository.count());
        logger.info("Created {} inventory entries", inventoryRepository.count());
    }

    private Company createCompany(String name, String loyaltyAccountId, String tier, String address) {
        Company company = new Company();
        company.setName(name);
        company.setLoyaltyAccountId(loyaltyAccountId);
        company.setTier(tier);
        company.setContactEmail(name.toLowerCase().replace(" ", "") + "@company.com");
        company.setContactPhone("555-" + (int)(Math.random() * 9000 + 1000));
        company.setAddress(address);
        company.setActive(true);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());
        return companyRepository.save(company);
    }

    private AccountManager createAccountManager(String firstName, String lastName, String email, Company company) {
        AccountManager accountManager = new AccountManager();
        accountManager.setFirstName(firstName);
        accountManager.setLastName(lastName);
        accountManager.setEmail(email);
        accountManager.setCompany(company);
        accountManager.setRole("ACCOUNT_MANAGER");
        accountManager.setActive(true);
        accountManager.setCreatedAt(LocalDateTime.now());
        accountManager.setUpdatedAt(LocalDateTime.now());
        return accountManagerRepository.save(accountManager);
    }

    private Product createProduct(String name, String description, ProductType productType, 
                                Integer pointsCost, String category, String brand, String imageUrl, boolean isActive) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setProductType(productType);
        product.setPointsCost(pointsCost);
        product.setCategory(category);
        product.setBrand(brand);
        product.setImageUrl(imageUrl);
        product.setActive(isActive);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    private void createInventoryEntry(Product product, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(quantity);
        inventory.setReservedQuantity(0);
        inventory.setReorderPoint(getReorderPoint(product.getProductType(), quantity));
        inventory.setLastRestocked(LocalDateTime.now());
        inventory.setCreatedAt(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    private int getRandomStock(int pointsCost) {
        // Higher point items have lower stock
        if (pointsCost > 50000) return (int)(Math.random() * 10 + 5);  // 5-14
        if (pointsCost > 30000) return (int)(Math.random() * 20 + 10); // 10-29
        if (pointsCost > 15000) return (int)(Math.random() * 50 + 25); // 25-74
        return (int)(Math.random() * 100 + 50); // 50-149
    }

    private int getReorderPoint(ProductType productType, int quantity) {
        if (productType == ProductType.VIRTUAL) return 0;
        return Math.max(5, quantity / 10); // 10% of stock or minimum 5
    }
}