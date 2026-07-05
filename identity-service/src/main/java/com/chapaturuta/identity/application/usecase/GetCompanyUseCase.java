package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.CompanyResponse;
import com.chapaturuta.identity.domain.model.Company;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface GetCompanyUseCase {
    CompanyResponse getCompanyByManagerId(UUID managerId);
}

@Service
class GetCompanyUseCaseImpl implements GetCompanyUseCase {
    private final CompanyRepository companyRepository;

    public GetCompanyUseCaseImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyResponse getCompanyByManagerId(UUID managerId) {
        Company company = companyRepository.findByManagerId(managerId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró empresa para el manager especificado."));
        return new CompanyResponse(company.getId(), company.getName(), company.getRuc());
    }
}
