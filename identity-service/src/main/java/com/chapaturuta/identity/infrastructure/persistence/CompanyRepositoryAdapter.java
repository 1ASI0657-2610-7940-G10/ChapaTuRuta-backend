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

    @Override
    public void deleteById(java.util.UUID id) {
        repository.deleteById(id);
    }

    @Override
    public java.util.Optional<Company> findByManagerId(java.util.UUID managerId) {
        return repository.findByManager_Id(managerId).map(entity -> 
            Company.builder()
                .id(entity.getId())
                .name(entity.getName())
                .ruc(entity.getRuc())
                .busPhotoUrl(entity.getBusPhotoUrl())
                .managerId(entity.getManager().getId())
                .build()
        );
    }
}