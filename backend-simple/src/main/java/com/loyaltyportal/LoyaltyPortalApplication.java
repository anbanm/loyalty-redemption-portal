package com.loyaltyportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.ResponseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootApplication
public class LoyaltyPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPortalApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // Initialize sample data
    @Bean
    CommandLineRunner init(ProductRepository productRepository, 
                          CompanyRepository companyRepository,
                          AccountManagerRepository accountManagerRepository,
                          UserRepository userRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                // Create companies
                Company acme = new Company();
                acme.setName("ACME Corporation");
                acme.setLoyaltyAccountId("ACME-123456");
                acme.setTier("GOLD");
                acme.setActive(true);
                acme.setCreatedAt(LocalDateTime.now());
                companyRepository.save(acme);

                // Create account managers
                AccountManager john = new AccountManager();
                john.setFirstName("John");
                john.setLastName("Doe");
                john.setEmail("john.doe@acme.com");
                john.setRole("ACCOUNT_MANAGER");
                john.setCompany(acme);
                john.setActive(true);
                john.setCreatedAt(LocalDateTime.now());
                accountManagerRepository.save(john);

                // Create sample products
                List<Product> products = Arrays.asList(
                    createProduct("Apple MacBook Pro 16\"", "Latest MacBook Pro with M3 chip", "PHYSICAL", 85000, "Electronics", "Apple", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400&h=300&fit=crop"),
                    createProduct("Sony WH-1000XM5 Headphones", "Premium noise-canceling wireless headphones", "PHYSICAL", 35000, "Electronics", "Sony", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&h=300&fit=crop"),
                    createProduct("Samsung 55\" QLED Smart TV", "4K QLED TV with HDR and smart features", "PHYSICAL", 65000, "Electronics", "Samsung", "https://images.unsplash.com/photo-1593784991095-a205069470b6?w=400&h=300&fit=crop"),
                    createProduct("Netflix Premium Subscription", "12-month Netflix Premium subscription", "VIRTUAL", 15000, "Entertainment", "Netflix", "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=400&h=300&fit=crop"),
                    createProduct("Spotify Premium Family", "6-month Spotify Premium Family plan", "VIRTUAL", 8000, "Entertainment", "Spotify", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&h=300&fit=crop"),
                    createProduct("Adobe Creative Cloud", "12-month Adobe Creative Cloud subscription", "VIRTUAL", 55000, "Software", "Adobe", "https://images.unsplash.com/photo-1611224923853-80b023f02d71?w=400&h=300&fit=crop"),
                    createProduct("Dyson V15 Vacuum Cleaner", "Cordless vacuum with laser dust detection", "PHYSICAL", 45000, "Home & Garden", "Dyson", "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop"),
                    createProduct("Nike Air Jordan Sneakers", "Limited edition Air Jordan basketball shoes", "PHYSICAL", 18000, "Fashion & Sports", "Nike", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=300&fit=crop"),
                    createProduct("Microsoft Office 365", "Annual Microsoft Office 365 Business Premium", "VIRTUAL", 32000, "Software", "Microsoft", "https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=300&fit=crop"),
                    createProduct("Fitbit Sense 2", "Advanced health and fitness smartwatch", "PHYSICAL", 25000, "Electronics", "Fitbit", "https://images.unsplash.com/photo-1544117519-31a4b719223d?w=400&h=300&fit=crop")
                );
                
                productRepository.saveAll(products);
                System.out.println("Initialized " + products.size() + " products with sample data");
                
                // Create sample users
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                
                // Admin user
                User admin = new User();
                admin.setEmail("admin@acme.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole("ADMIN");
                admin.setCompany(acme);
                admin.setActive(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepository.save(admin);
                
                // Regular user
                User user = new User();
                user.setEmail("user@acme.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setFirstName("John");
                user.setLastName("Smith");
                user.setRole("USER");
                user.setCompany(acme);
                user.setActive(true);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                System.out.println("Created sample users - admin@acme.com (admin123) and user@acme.com (user123)");
            }
        };
    }

    private Product createProduct(String name, String description, String type, Integer points, String category, String brand, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setProductType(type);
        product.setPointsCost(points);
        product.setCategory(category);
        product.setBrand(brand);
        product.setImageUrl(imageUrl);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        return product;
    }
}

// Entities
@Entity
@Table(name = "products")
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank
    private String productType; // PHYSICAL or VIRTUAL

    @Min(1)
    private Integer pointsCost;

    private String category;
    private String brand;
    private String imageUrl;
    private Boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors, getters, and setters
    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public Integer getPointsCost() { return pointsCost; }
    public void setPointsCost(Integer pointsCost) { this.pointsCost = pointsCost; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

@Entity
@Table(name = "companies")
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;
    
    private String loyaltyAccountId;
    private String tier;
    private Boolean active = true;
    private LocalDateTime createdAt;

    // Constructors, getters, and setters
    public Company() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLoyaltyAccountId() { return loyaltyAccountId; }
    public void setLoyaltyAccountId(String loyaltyAccountId) { this.loyaltyAccountId = loyaltyAccountId; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

@Entity
@Table(name = "account_managers")
class AccountManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private Boolean active = true;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    // Constructors, getters, and setters
    public AccountManager() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    private String firstName;
    private String lastName;

    @NotBlank
    private String role; // ADMIN, USER

    private Boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    // Constructors, getters, and setters
    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
}

// Repositories
interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategory(String category);
    List<Product> findByBrand(String brand);
    List<Product> findByProductType(String productType);
}

interface CompanyRepository extends JpaRepository<Company, Long> {
}

interface AccountManagerRepository extends JpaRepository<AccountManager, Long> {
}

interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findByCompanyId(Long companyId);
    List<User> findByRole(String role);
}

// Controllers
@RestController
@RequestMapping("/products")
class ProductController {
    
    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(product -> ResponseEntity.ok(product))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productRepository.findByNameContainingIgnoreCase(q));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.asList("Electronics", "Home & Garden", "Fashion & Sports", "Entertainment", "Software");
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getBrands() {
        List<String> brands = Arrays.asList("Apple", "Sony", "Samsung", "Netflix", "Spotify", "Adobe", "Dyson", "Nike", "Microsoft", "Fitbit");
        return ResponseEntity.ok(brands);
    }
}

@RestController
@RequestMapping("/companies")
class CompanyController {
    
    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable Long id) {
        return companyRepository.findById(id)
            .map(company -> ResponseEntity.ok(company))
            .orElse(ResponseEntity.notFound().build());
    }
}

// Order Entity
@Entity
@Table(name = "orders")
class RedemptionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String orderNumber;
    private String status = "PENDING"; // PENDING, APPROVED_LEVEL_1, APPROVED_LEVEL_2, APPROVED_FINAL, REJECTED, SHIPPED, DELIVERED
    private String workflowStage = "LEVEL_1"; // LEVEL_1, LEVEL_2, COMPLETED
    private Integer totalPoints;
    private Integer totalItems;
    private String shippingAddress;
    private String specialInstructions;
    private String rejectionReason;
    private String orderType; // PHYSICAL, VIRTUAL
    private LocalDateTime createdAt;
    private LocalDateTime level1ApprovedAt;
    private LocalDateTime level2ApprovedAt;
    private LocalDateTime finalApprovedAt;
    private LocalDateTime shippedAt;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "account_manager_id") 
    private AccountManager accountManager;

    // Constructors, getters, and setters
    public RedemptionOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getWorkflowStage() { return workflowStage; }
    public void setWorkflowStage(String workflowStage) { this.workflowStage = workflowStage; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public LocalDateTime getLevel1ApprovedAt() { return level1ApprovedAt; }
    public void setLevel1ApprovedAt(LocalDateTime level1ApprovedAt) { this.level1ApprovedAt = level1ApprovedAt; }

    public LocalDateTime getLevel2ApprovedAt() { return level2ApprovedAt; }
    public void setLevel2ApprovedAt(LocalDateTime level2ApprovedAt) { this.level2ApprovedAt = level2ApprovedAt; }

    public LocalDateTime getFinalApprovedAt() { return finalApprovedAt; }
    public void setFinalApprovedAt(LocalDateTime finalApprovedAt) { this.finalApprovedAt = finalApprovedAt; }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public AccountManager getAccountManager() { return accountManager; }
    public void setAccountManager(AccountManager accountManager) { this.accountManager = accountManager; }
}

interface OrderRepository extends JpaRepository<RedemptionOrder, Long> {
    List<RedemptionOrder> findByStatus(String status);
    List<RedemptionOrder> findByStatusOrderByCreatedAtDesc(String status);
    List<RedemptionOrder> findByWorkflowStage(String workflowStage);
    List<RedemptionOrder> findByWorkflowStageOrderByCreatedAtDesc(String workflowStage);
    List<RedemptionOrder> findAllByOrderByCreatedAtDesc();
}

// Workflow Approval Entity
@Entity
@Table(name = "order_approvals")
class OrderApproval {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private RedemptionOrder order;
    
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;
    
    private String approvalLevel; // LEVEL_1, LEVEL_2
    private String action; // APPROVED, REJECTED
    private String comments;
    private LocalDateTime actionAt;
    
    // Constructors, getters, and setters
    public OrderApproval() {}
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public RedemptionOrder getOrder() { return order; }
    public void setOrder(RedemptionOrder order) { this.order = order; }
    
    public User getApprover() { return approver; }
    public void setApprover(User approver) { this.approver = approver; }
    
    public String getApprovalLevel() { return approvalLevel; }
    public void setApprovalLevel(String approvalLevel) { this.approvalLevel = approvalLevel; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public LocalDateTime getActionAt() { return actionAt; }
    public void setActionAt(LocalDateTime actionAt) { this.actionAt = actionAt; }
}

interface OrderApprovalRepository extends JpaRepository<OrderApproval, Long> {
    List<OrderApproval> findByOrderId(Long orderId);
    List<OrderApproval> findByOrderIdOrderByActionAtDesc(Long orderId);
}

@RestController
@RequestMapping("/redemption")
class RedemptionController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private AccountManagerRepository accountManagerRepository;
    
    @GetMapping("/balance/{companyId}")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long companyId) {
        // Mock balance response
        Map<String, Object> response = new HashMap<>();
        response.put("balance", 150000);
        response.put("availableBalance", 150000);
        response.put("accountId", "ACME-123456");
        response.put("tier", "GOLD");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders")
    @Transactional
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        try {
            // Input validation
            if (orderRequest == null) {
                throw new RuntimeException("Order request cannot be null");
            }
            if (orderRequest.get("totalPoints") == null || (Integer) orderRequest.get("totalPoints") <= 0) {
                throw new RuntimeException("Invalid total points");
            }
            if (orderRequest.get("items") == null || ((List<?>) orderRequest.get("items")).isEmpty()) {
                throw new RuntimeException("Order must contain at least one item");
            }
            if (orderRequest.get("companyId") == null || orderRequest.get("accountManagerId") == null) {
                throw new RuntimeException("Company ID and Account Manager ID are required");
            }
            RedemptionOrder order = new RedemptionOrder();
            order.setOrderNumber("ORD-" + System.currentTimeMillis());
            order.setStatus("PENDING");
            order.setTotalPoints((Integer) orderRequest.get("totalPoints"));
            order.setTotalItems(((List<?>) orderRequest.get("items")).size());
            order.setCreatedAt(LocalDateTime.now());
            
            // Set company and account manager with null checks
            Long companyId = ((Number) orderRequest.get("companyId")).longValue();
            Long accountManagerId = ((Number) orderRequest.get("accountManagerId")).longValue();
            
            Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
            AccountManager accountManager = accountManagerRepository.findById(accountManagerId)
                .orElseThrow(() -> new RuntimeException("Account Manager not found with id: " + accountManagerId));
            
            order.setCompany(company);
            order.setAccountManager(accountManager);
            
            if (orderRequest.get("shippingAddress") != null) {
                order.setShippingAddress((String) orderRequest.get("shippingAddress"));
            }
            if (orderRequest.get("specialInstructions") != null) {
                order.setSpecialInstructions((String) orderRequest.get("specialInstructions"));
            }
            
            order = orderRepository.save(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orderNumber", order.getOrderNumber());
            response.put("status", order.getStatus());
            response.put("totalPoints", order.getTotalPoints());
            response.put("message", "Order created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order creation failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

@RestController
@RequestMapping("/admin")
class AdminController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderApprovalRepository orderApprovalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<List<RedemptionOrder>> getAllOrders() {
        // TODO: Add proper authentication/authorization check
        // For now, this is a demo endpoint - should verify admin role
        return ResponseEntity.ok(orderRepository.findAllByOrderByCreatedAtDesc());
    }
    
    @GetMapping("/orders/pending")
    public ResponseEntity<List<RedemptionOrder>> getPendingOrders() {
        return ResponseEntity.ok(orderRepository.findByStatusOrderByCreatedAtDesc("PENDING"));
    }
    
    @PostMapping("/orders/{orderId}/approve")
    public ResponseEntity<Map<String, Object>> approveOrder(@PathVariable Long orderId, @RequestBody Map<String, Object> request) {
        return orderRepository.findById(orderId)
            .map(order -> {
                String currentStage = order.getWorkflowStage();
                String comments = (String) request.get("comments");
                // For demo purposes, use admin user ID 1
                Long approverId = 1L;
                
                if ("LEVEL_1".equals(currentStage)) {
                    order.setStatus("APPROVED_LEVEL_1");
                    order.setWorkflowStage("LEVEL_2");
                    order.setLevel1ApprovedAt(LocalDateTime.now());
                    
                    // Create approval record
                    createApprovalRecord(orderId, approverId, "LEVEL_1", "APPROVED", comments);
                    
                } else if ("LEVEL_2".equals(currentStage)) {
                    order.setStatus("APPROVED_FINAL");
                    order.setWorkflowStage("COMPLETED");
                    order.setLevel2ApprovedAt(LocalDateTime.now());
                    order.setFinalApprovedAt(LocalDateTime.now());
                    
                    // Create approval record
                    createApprovalRecord(orderId, approverId, "LEVEL_2", "APPROVED", comments);
                    
                    // Process order based on type
                    processApprovedOrder(order);
                }
                
                orderRepository.save(order);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Order approved at " + currentStage + " successfully");
                response.put("orderNumber", order.getOrderNumber());
                response.put("nextStage", order.getWorkflowStage());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/orders/{orderId}/reject")
    public ResponseEntity<Map<String, Object>> rejectOrder(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        return orderRepository.findById(orderId)
            .map(order -> {
                order.setStatus("REJECTED");
                order.setRejectionReason(request.get("reason"));
                orderRepository.save(order);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Order rejected successfully");
                response.put("orderNumber", order.getOrderNumber());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/orders/{orderId}/ship")
    public ResponseEntity<Map<String, Object>> markAsShipped(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
            .map(order -> {
                order.setStatus("SHIPPED");
                order.setShippedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Order marked as shipped");
                response.put("orderNumber", order.getOrderNumber());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Product Management
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(productRepository.save(product));
    }
    
    @PutMapping("/products/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product productData) {
        return productRepository.findById(productId)
            .map(product -> {
                product.setName(productData.getName());
                product.setDescription(productData.getDescription());
                product.setPointsCost(productData.getPointsCost());
                product.setCategory(productData.getCategory());
                product.setBrand(productData.getBrand());
                product.setImageUrl(productData.getImageUrl());
                product.setActive(productData.getActive());
                product.setUpdatedAt(LocalDateTime.now());
                return ResponseEntity.ok(productRepository.save(product));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        return productRepository.findById(productId)
            .map(product -> {
                product.setActive(false);
                productRepository.save(product);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Product deactivated successfully");
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("activeProducts", productRepository.findByActiveTrue().size());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.findByStatus("PENDING").size());
        stats.put("approvedOrders", orderRepository.findByStatus("APPROVED_FINAL").size());
        return ResponseEntity.ok(stats);
    }
    
    // Helper methods for workflow
    private void createApprovalRecord(Long orderId, Long approverId, String level, String action, String comments) {
        try {
            OrderApproval approval = new OrderApproval();
            approval.setOrder(orderRepository.findById(orderId).orElse(null));
            approval.setApprover(userRepository.findById(approverId).orElse(null));
            approval.setApprovalLevel(level);
            approval.setAction(action);
            approval.setComments(comments);
            approval.setActionAt(LocalDateTime.now());
            orderApprovalRepository.save(approval);
        } catch (Exception e) {
            System.err.println("Error creating approval record: " + e.getMessage());
        }
    }
    
    private void processApprovedOrder(RedemptionOrder order) {
        if ("VIRTUAL".equals(order.getOrderType())) {
            // Process virtual order automatically
            order.setStatus("DELIVERED");
            System.out.println("Virtual order " + order.getOrderNumber() + " processed automatically");
            // Here you would integrate with virtual product delivery systems
        } else {
            // Physical orders require manual processing
            order.setStatus("APPROVED_FINAL");
            System.out.println("Physical order " + order.getOrderNumber() + " ready for manual processing");
            // Here you would send email notifications to fulfillment team
        }
    }
    
    @GetMapping("/orders/workflow/{stage}")
    public ResponseEntity<List<RedemptionOrder>> getOrdersByWorkflowStage(@PathVariable String stage) {
        return ResponseEntity.ok(orderRepository.findByWorkflowStageOrderByCreatedAtDesc(stage));
    }
    
    @GetMapping("/orders/{orderId}/approvals")
    public ResponseEntity<List<OrderApproval>> getOrderApprovals(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderApprovalRepository.findByOrderIdOrderByActionAtDesc(orderId));
    }
}

// Authentication Service
@Service
class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User registerUser(Map<String, Object> registrationData) throws RuntimeException {
        String email = (String) registrationData.get("email");
        
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode((String) registrationData.get("password")));
        user.setFirstName((String) registrationData.get("firstName"));
        user.setLastName((String) registrationData.get("lastName"));
        user.setRole((String) registrationData.getOrDefault("role", "USER"));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        // Associate with company if provided
        if (registrationData.get("companyId") != null) {
            Long companyId = ((Number) registrationData.get("companyId")).longValue();
            user.setCompany(companyRepository.findById(companyId).orElse(null));
        }
        
        return userRepository.save(user);
    }
    
    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getActive() && passwordEncoder.matches(password, user.getPassword())) {
            user.setLastLoginAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        return null;
    }
}

// Authentication Controller
@RestController
@RequestMapping("/auth")
class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> registrationData) {
        try {
            User user = authService.registerUser(registrationData);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");
        
        User user = authService.authenticateUser(email, password);
        Map<String, Object> response = new HashMap<>();
        
        if (user != null) {
            response.put("message", "Login successful");
            response.put("user", createUserResponse(user));
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @GetMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long userId) {
        return userRepository.findById(userId)
            .map(user -> ResponseEntity.ok(createUserResponse(user)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("role", user.getRole());
        userResponse.put("active", user.getActive());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        if (user.getCompany() != null) {
            userResponse.put("company", createCompanyResponse(user.getCompany()));
        }
        return userResponse;
    }
    
    private Map<String, Object> createCompanyResponse(Company company) {
        Map<String, Object> companyResponse = new HashMap<>();
        companyResponse.put("id", company.getId());
        companyResponse.put("name", company.getName());
        companyResponse.put("loyaltyAccountId", company.getLoyaltyAccountId());
        companyResponse.put("tier", company.getTier());
        return companyResponse;
    }
}