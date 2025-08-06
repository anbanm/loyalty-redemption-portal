package com.loyaltyportal.repository;

import com.loyaltyportal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByLoyaltyAccountId(String loyaltyAccountId);

    List<Company> findByIsActiveTrue();

    List<Company> findByTierLevel(String tierLevel);

    @Query("SELECT c FROM Company c WHERE c.name LIKE %:name% AND c.isActive = true")
    List<Company> findActiveCompaniesByNameContaining(@Param("name") String name);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.isActive = true")
    long countActiveCompanies();

    boolean existsByLoyaltyAccountId(String loyaltyAccountId);

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.accountManagers WHERE c.id = :id")
    Optional<Company> findByIdWithAccountManagers(@Param("id") UUID id);
}