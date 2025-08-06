package com.loyaltyportal.repository;

import com.loyaltyportal.entity.Product;
import com.loyaltyportal.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    List<Product> findByIsActiveTrue();

    Page<Product> findByIsActiveTrue(Pageable pageable);

    List<Product> findByProductTypeAndIsActiveTrue(ProductType productType);

    Page<Product> findByProductTypeAndIsActiveTrue(ProductType productType, Pageable pageable);

    List<Product> findByCategoryAndIsActiveTrue(String category);

    List<Product> findByBrandAndIsActiveTrue(String brand);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true")
    List<Product> findActiveProductsByNameContaining(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.pointsCost BETWEEN :minPoints AND :maxPoints AND p.isActive = true")
    List<Product> findActiveProductsByPointsRange(@Param("minPoints") Integer minPoints, @Param("maxPoints") Integer maxPoints);

    @Query("SELECT p FROM Product p WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND p.isActive = true")
    Page<Product> searchActiveProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findAllActiveCategories();

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.isActive = true ORDER BY p.brand")
    List<String> findAllActiveBrands();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.productType = :productType AND p.isActive = true")
    long countActiveProductsByType(@Param("productType") ProductType productType);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.inventories WHERE p.id = :id AND p.isActive = true")
    Optional<Product> findActiveProductWithInventory(@Param("id") UUID id);

    @Query("SELECT p FROM Product p JOIN p.inventories i WHERE i.quantityAvailable > 0 AND p.isActive = true")
    List<Product> findActiveProductsInStock();
}