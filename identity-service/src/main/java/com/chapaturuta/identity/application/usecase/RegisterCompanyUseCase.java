package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.CompanyRegistrationRequest;
import com.chapaturuta.identity.application.dto.CompanyResponse;
import com.chapaturuta.identity.domain.model.Company;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

public interface RegisterCompanyUseCase {
    CompanyResponse registerCompany(CompanyRegistrationRequest request);
}

@Service
class RegisterCompanyUseCaseImpl implements RegisterCompanyUseCase {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public RegisterCompanyUseCaseImpl(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CompanyResponse registerCompany(CompanyRegistrationRequest request) {
        if (request.ruc() == null || !request.ruc().matches("^(10|15|20)\\d{9}$")) {
            throw new IllegalArgumentException("Formato de RUC inválido. Debe tener 11 números y empezar con 10, 15 o 20.");
        }

        if (companyRepository.existsByRuc(request.ruc())) {
            throw new IllegalArgumentException("El RUC ya está registrado.");
        }

        User manager = userRepository.findById(request.managerId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario gestor no encontrado."));

        if (manager.getRole() != Role.MANAGER) {
            throw new IllegalArgumentException("El usuario debe tener rol MANAGER para registrar una empresa.");
        }

        Company company = Company.builder()
                .name(request.name())
                .ruc(request.ruc())
                .busPhotoUrl(request.busPhotoUrl())
                .managerId(request.managerId())
                .build();

        Company saved = companyRepository.save(company);
        return new CompanyResponse(saved.getId(), saved.getName(), saved.getRuc());
    }


}