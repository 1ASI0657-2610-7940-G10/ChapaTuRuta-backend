package com.chapaturuta.identity.infrastructure.persistence;

import com.chapaturuta.identity.domain.model.Company;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import org.springframework.stereotype.Component;

@Component
public class CompanyRepositoryAdapter implements CompanyRepository {

    private final SpringDataCompanyRepository repository;

    public CompanyRepositoryAdapter(SpringDataCompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Company save(Company company) {
        CompanyEntity entity = CompanyEntity.builder()
                .id(company.getId())
                .name(company.getName())
                .busPhotoUrl(company.getBusPhotoUrl())
                .ruc(company.getRuc())
                .manager(UserEntity.builder().id(company.getManagerId()).build())
                .build();

        CompanyEntity saved = repository.save(entity);
        company.setId(saved.getId());
        return company;
    }

    @Override
    public boolean existsByRuc(String ruc) {
        return repository.existsByRuc(ruc);
    }
}