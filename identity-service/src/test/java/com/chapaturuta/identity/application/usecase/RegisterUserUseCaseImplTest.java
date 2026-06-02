package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserUseCaseImpl registerUserUseCase;

    private UserRegistrationRequest validRequest;
    private UUID expectedUserId;
    private UUID expectedCompanyId;

    @BeforeEach
    void setUp() {
        expectedUserId = UUID.randomUUID();
        expectedCompanyId = UUID.randomUUID();

        validRequest = new UserRegistrationRequest(
                "Carlos Mendoza",
                "carlos@example.com",
                "password123",
                Role.DRIVER,
                expectedCompanyId
        );
    }

    @Test
    void registerUser_Successful_ReturnsUserResponse() {
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.empty());

        User savedMockUser = User.builder()
                .id(expectedUserId)
                .name(validRequest.name())
                .email(validRequest.email())
                .password(validRequest.password())
                .role(validRequest.role())
                .companyId(validRequest.companyId())
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedMockUser);

        UserResponse response = registerUserUseCase.registerUser(validRequest);

        assertNotNull(response);
        assertEquals(expectedUserId, response.id());
        assertEquals("carlos@example.com", response.email());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsIllegalArgumentException() {
        User existingUser = User.builder().email(validRequest.email()).build();
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerUserUseCase.registerUser(validRequest)
        );

        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_DriverWithoutCompanyId_ThrowsIllegalArgumentException() {
        UserRegistrationRequest badRequest = new UserRegistrationRequest(
                "Pedro Conductor",
                "pedro@example.com",
                "pass123",
                Role.DRIVER,
                null
        );

        when(userRepository.findByEmail(badRequest.email())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerUserUseCase.registerUser(badRequest)
        );

        assertEquals("Los conductores deben estar asociados a una empresa (companyId requerido)", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}