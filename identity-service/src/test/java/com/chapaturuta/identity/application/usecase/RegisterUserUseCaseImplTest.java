package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
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

    @Test
    void registerUser_Successful_ReturnsUserResponse() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest("Juan", "juan@correo.com", "12345", Role.PASSENGER, null);
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name("Juan")
                .email("juan@correo.com")
                .role(Role.PASSENGER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = registerUserUseCase.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("Juan", response.name());
        assertEquals("juan@correo.com", response.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        UserRegistrationRequest request = new UserRegistrationRequest("Juan", "existe@correo.com", "12345", Role.PASSENGER, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registerUserUseCase.registerUser(request);
        });
        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class)); // Asegura que nunca intente guardar
    }

    @Test
    void registerUser_DriverWithoutCompany_ThrowsException() {
        // Arrange
        // Se intenta registrar un DRIVER, pero el companyId es null
        UserRegistrationRequest request = new UserRegistrationRequest("Pedro Chofer", "pedro@correo.com", "12345", Role.DRIVER, null);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registerUserUseCase.registerUser(request);
        });
        assertEquals("Los conductores deben estar asociados a una empresa (companyId requerido)", exception.getMessage());
    }
}