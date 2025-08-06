package com.loyaltyportal.controller;

import com.loyaltyportal.entity.Company;
import com.loyaltyportal.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@Tag(name = "Companies", description = "Company management operations")
public class CompanyController {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping
    @Operation(summary = "Get all active companies", description = "Retrieve list of all active companies")
    public ResponseEntity<List<Company>> getAllActiveCompanies() {
        List<Company> companies = companyRepository.findByIsActiveTrue();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID", description = "Retrieve a specific company by its ID")
    public ResponseEntity<Company> getCompanyById(
            @Parameter(description = "Company ID") @PathVariable UUID id) {
        return companyRepository.findById(id)
                .filter(Company::getIsActive)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-managers")
    @Operation(summary = "Get company with account managers", 
               description = "Retrieve a company along with its account managers")
    public ResponseEntity<Company> getCompanyWithAccountManagers(
            @Parameter(description = "Company ID") @PathVariable UUID id) {
        return companyRepository.findByIdWithAccountManagers(id)
                .filter(Company::getIsActive)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loyalty-account/{loyaltyAccountId}")
    @Operation(summary = "Get company by loyalty account ID", 
               description = "Retrieve a company by its loyalty account ID")
    public ResponseEntity<Company> getCompanyByLoyaltyAccountId(
            @Parameter(description = "Loyalty Account ID") @PathVariable String loyaltyAccountId) {
        return companyRepository.findByLoyaltyAccountId(loyaltyAccountId)
                .filter(Company::getIsActive)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tier/{tierLevel}")
    @Operation(summary = "Get companies by tier level", 
               description = "Retrieve companies filtered by their tier level")
    public ResponseEntity<List<Company>> getCompaniesByTier(
            @Parameter(description = "Tier level") @PathVariable String tierLevel) {
        List<Company> companies = companyRepository.findByTierLevel(tierLevel);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Search companies by name")
    public ResponseEntity<List<Company>> searchCompanies(
            @Parameter(description = "Company name search term") @RequestParam String name) {
        List<Company> companies = companyRepository.findActiveCompaniesByNameContaining(name);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/count")
    @Operation(summary = "Get active companies count", description = "Get the total count of active companies")
    public ResponseEntity<Long> getActiveCompaniesCount() {
        long count = companyRepository.countActiveCompanies();
        return ResponseEntity.ok(count);
    }
}