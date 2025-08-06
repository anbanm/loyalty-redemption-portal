package com.loyaltyportal.repository;

import com.loyaltyportal.entity.AccountManager;
import com.loyaltyportal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountManagerRepository extends JpaRepository<AccountManager, UUID> {

    Optional<AccountManager> findByEmail(String email);

    List<AccountManager> findByCompanyAndIsActiveTrue(Company company);

    List<AccountManager> findByCompanyIdAndIsActiveTrue(UUID companyId);

    List<AccountManager> findByIsActiveTrue();

    List<AccountManager> findByRole(String role);

    @Query("SELECT am FROM AccountManager am WHERE am.company.id = :companyId AND am.isActive = true")
    List<AccountManager> findActiveAccountManagersByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(am) FROM AccountManager am WHERE am.company.id = :companyId AND am.isActive = true")
    long countActiveAccountManagersByCompany(@Param("companyId") UUID companyId);

    boolean existsByEmail(String email);

    @Query("SELECT am FROM AccountManager am LEFT JOIN FETCH am.company WHERE am.id = :id")
    Optional<AccountManager> findByIdWithCompany(@Param("id") UUID id);

    @Query("SELECT am FROM AccountManager am WHERE am.email = :email AND am.isActive = true")
    Optional<AccountManager> findActiveByEmail(@Param("email") String email);
}