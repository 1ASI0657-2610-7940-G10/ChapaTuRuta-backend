package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticateUserUseCaseImpl authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticateUserUseCase, "jwtSecret", "ChapaTuRutaSecretKeyParaFirmarLosTokensJWTDeFormaSegura2026");
    }

    @Test
    void authenticate_Successful_ReturnsJwtToken() {
        // Arrange
        LoginRequest request = new LoginRequest("test@correo.com", "password123");
        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@correo.com")
                .password("password123")
                .role(Role.PASSENGER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));

        // Act
        String token = authenticateUserUseCase.authenticate(request);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(userRepository, times(1)).findByEmail(request.email());
    }

    @Test
    void authenticate_UserNotFound_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("noexiste@correo.com", "password123");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticateUserUseCase.authenticate(request);
        });
        assertEquals("El usuario no existe", exception.getMessage());
    }

    @Test
    void authenticate_WrongPassword_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("test@correo.com", "wrongpass");
        User mockUser = User.builder().email("test@correo.com").password("correctpass").build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticateUserUseCase.authenticate(request);
        });
        assertEquals("Credenciales inválidas", exception.getMessage());
    }
}