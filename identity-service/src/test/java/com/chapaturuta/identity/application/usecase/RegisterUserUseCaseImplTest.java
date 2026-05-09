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

    @BeforeEach
    void setUp() {
        expectedUserId = UUID.randomUUID();
        // Pasamos Role.DRIVER (Enum) en lugar de un String
        validRequest = new UserRegistrationRequest(
                "Carlos Mendoza",
                "carlos@example.com",
                "password123",
                Role.DRIVER
        );
    }

    @Test
    void registerUser_Successful_ReturnsUserResponse() {
        // Arrange: Simulamos que el correo no existe
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.empty());

        // Usamos UUID en lugar de 1L
        User savedMockUser = User.builder()
                .id(expectedUserId)
                .name(validRequest.name())
                .email(validRequest.email())
                .password(validRequest.password())
                .role(validRequest.role())
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedMockUser);

        // Act
        UserResponse response = registerUserUseCase.registerUser(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(expectedUserId, response.id());
        assertEquals("carlos@example.com", response.email());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsIllegalArgumentException() {
        // Arrange: Simulamos que el correo ya está registrado
        User existingUser = User.builder().email(validRequest.email()).build();
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registerUserUseCase.registerUser(validRequest)
        );

        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(validRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }
}