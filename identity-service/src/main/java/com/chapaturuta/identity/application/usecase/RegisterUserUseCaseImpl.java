package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        // Regla de Negocio: Si es conductor, debe enviar una empresa a la que pertenece
        if (request.role() == Role.DRIVER && request.companyId() == null) {
            throw new IllegalArgumentException("Los conductores deben estar asociados a una empresa (companyId requerido)");
        }

        User newUser = User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .role(request.role())
                .companyId(request.companyId())
                .routeId(request.routeId())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCompanyId(),
                savedUser.getRouteId(),
                savedUser.getCreatedAt()
        );
    }
}