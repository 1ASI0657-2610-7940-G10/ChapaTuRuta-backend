package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.CompanyRegistrationRequest;
import com.chapaturuta.identity.application.dto.CompanyResponse;
import com.chapaturuta.identity.domain.model.Company;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterCompanyUseCaseImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterCompanyUseCaseImpl registerCompanyUseCase;

    @Test
    void registerCompany_WhenManagerAndRucAreValid_SavesCompany() {
        UUID managerId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        CompanyRegistrationRequest request = new CompanyRegistrationRequest(
                "Empresa Lima",
                "20123456789",
                "https://example.com/bus.png",
                managerId
        );

        when(companyRepository.existsByRuc("20123456789")).thenReturn(false);
        when(userRepository.findById(managerId)).thenReturn(Optional.of(User.builder().id(managerId).role(Role.MANAGER).build()));
        when(companyRepository.save(any(Company.class))).thenReturn(Company.builder()
                .id(companyId)
                .name("Empresa Lima")
                .ruc("20123456789")
                .managerId(managerId)
                .build());

        CompanyResponse response = registerCompanyUseCase.registerCompany(request);

        assertEquals(companyId, response.id());
        assertEquals("Empresa Lima", response.name());
        assertEquals("20123456789", response.ruc());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    void registerCompany_WhenRucFormatIsInvalid_ThrowsException() {
        CompanyRegistrationRequest request = new CompanyRegistrationRequest(
                "Empresa Lima",
                "99123456789",
                null,
                UUID.randomUUID()
        );

        assertThrows(IllegalArgumentException.class, () -> registerCompanyUseCase.registerCompany(request));

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void registerCompany_WhenUserIsNotManager_ThrowsException() {
        UUID userId = UUID.randomUUID();
        CompanyRegistrationRequest request = new CompanyRegistrationRequest("Empresa Lima", "20123456789", null, userId);

        when(companyRepository.existsByRuc("20123456789")).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).role(Role.PASSENGER).build()));

        assertThrows(IllegalArgumentException.class, () -> registerCompanyUseCase.registerCompany(request));

        verify(companyRepository, never()).save(any(Company.class));
    }
}
