package com.loyaltyportal.controller;

import com.loyaltyportal.entity.Product;
import com.loyaltyportal.entity.ProductType;
import com.loyaltyportal.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductRepository productRepository;

    @Autowired
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    @Operation(summary = "Get all active products", description = "Retrieve paginated list of active products")
    public ResponseEntity<Page<Product>> getAllProducts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        return productRepository.findById(id)
                .filter(Product::getIsActive)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, description, category, or brand")
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "Search term") @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productRepository.searchActiveProducts(q, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/type/{productType}")
    @Operation(summary = "Get products by type", description = "Retrieve products filtered by type (PHYSICAL or VIRTUAL)")
    public ResponseEntity<Page<Product>> getProductsByType(
            @Parameter(description = "Product type") @PathVariable ProductType productType,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Product> products = productRepository.findByProductTypeAndIsActiveTrue(productType, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieve products in a specific category")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @Parameter(description = "Product category") @PathVariable String category) {
        List<Product> products = productRepository.findByCategoryAndIsActiveTrue(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brand/{brand}")
    @Operation(summary = "Get products by brand", description = "Retrieve products from a specific brand")
    public ResponseEntity<List<Product>> getProductsByBrand(
            @Parameter(description = "Product brand") @PathVariable String brand) {
        List<Product> products = productRepository.findByBrandAndIsActiveTrue(brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve list of all available product categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productRepository.findAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/brands")
    @Operation(summary = "Get all brands", description = "Retrieve list of all available product brands")
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productRepository.findAllActiveBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/points-range")
    @Operation(summary = "Get products by points range", description = "Retrieve products within a specific points cost range")
    public ResponseEntity<List<Product>> getProductsByPointsRange(
            @Parameter(description = "Minimum points") @RequestParam Integer minPoints,
            @Parameter(description = "Maximum points") @RequestParam Integer maxPoints) {
        List<Product> products = productRepository.findActiveProductsByPointsRange(minPoints, maxPoints);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    @Operation(summary = "Get in-stock products", description = "Retrieve products that are currently in stock")
    public ResponseEntity<List<Product>> getInStockProducts() {
        List<Product> products = productRepository.findActiveProductsInStock();
        return ResponseEntity.ok(products);
    }
}