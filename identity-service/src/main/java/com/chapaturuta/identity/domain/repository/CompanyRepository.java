package com.chapaturuta.identity.domain.repository;
import com.chapaturuta.identity.domain.model.Company;

public interface CompanyRepository {
    Company save(Company company);
    boolean existsByRuc(String ruc);
    void deleteById(java.util.UUID id);
    java.util.Optional<Company> findByManagerId(java.util.UUID managerId);
}