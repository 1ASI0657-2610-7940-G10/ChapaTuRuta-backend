package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.dto.UserUpdateRequest;
import com.chapaturuta.identity.domain.model.Company;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ManageUserUseCaseImpl manageUserUseCase;

    private UUID userId;
    private User passenger;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        passenger = User.builder()
                .id(userId)
                .name("Adrian")
                .email("adrian@mail.com")
                .password("old-password")
                .role(Role.PASSENGER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserProfile_WhenUserExists_ReturnsProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(passenger));

        UserResponse response = manageUserUseCase.getUserProfile(userId);

        assertEquals(userId, response.id());
        assertEquals("Adrian", response.name());
        assertEquals("adrian@mail.com", response.email());
    }

    @Test
    void updateUserProfile_UpdatesOnlyProvidedFields() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(passenger));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = manageUserUseCase.updateUserProfile(userId, new UserUpdateRequest("Adrian Ruiz", ""));

        assertEquals("Adrian Ruiz", response.name());
        assertEquals("old-password", passenger.getPassword());
        verify(userRepository).save(passenger);
    }

    @Test
    void deleteUser_WhenManagerOwnsCompany_DeletesCompanyDriversAndCompany() {
        UUID companyId = UUID.randomUUID();
        User manager = User.builder().id(userId).role(Role.MANAGER).build();
        Company company = Company.builder().id(companyId).managerId(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(manager));
        when(companyRepository.findByManagerId(userId)).thenReturn(Optional.of(company));

        manageUserUseCase.deleteUser(userId);

        verify(userRepository).deleteByCompanyId(companyId);
        verify(companyRepository).deleteById(companyId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ThrowsExceptionAndDoesNotDelete() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> manageUserUseCase.deleteUser(userId));

        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void getDriversByCompany_ReturnsOnlyDriversFromRepository() {
        UUID companyId = UUID.randomUUID();
        User driver = User.builder()
                .id(UUID.randomUUID())
                .name("Driver")
                .email("driver@mail.com")
                .role(Role.DRIVER)
                .companyId(companyId)
                .build();

        when(userRepository.findByCompanyIdAndRole(companyId, Role.DRIVER)).thenReturn(List.of(driver));

        List<UserResponse> drivers = manageUserUseCase.getDriversByCompany(companyId);

        assertEquals(1, drivers.size());
        assertEquals(Role.DRIVER, drivers.get(0).role());
        assertTrue(drivers.stream().allMatch(response -> companyId.equals(response.companyId())));
    }
}
