package com.chapaturuta.identity.domain.repository;
import com.chapaturuta.identity.domain.model.Company;

public interface CompanyRepository {
    Company save(Company company);
    boolean existsByRuc(String ruc);
}