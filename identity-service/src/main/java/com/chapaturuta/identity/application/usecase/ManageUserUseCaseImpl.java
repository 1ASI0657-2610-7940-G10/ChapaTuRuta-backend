package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.dto.UserUpdateRequest;
import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import com.chapaturuta.identity.domain.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import com.chapaturuta.identity.domain.model.Company;

@Service
public class ManageUserUseCaseImpl implements ManageUserUseCase {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public ManageUserUseCaseImpl(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUserProfile(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(request.password());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
                
        if (user.getRole() == Role.MANAGER) {
            companyRepository.findByManagerId(userId).ifPresent(company -> {
                userRepository.deleteByCompanyId(company.getId());
                companyRepository.deleteById(company.getId());
            });
        }
        
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserResponse> getDriversByCompany(UUID companyId) {
        return userRepository.findByCompanyIdAndRole(companyId, Role.DRIVER).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompanyId(),
                user.getRouteId(),
                user.getCreatedAt()
        );
    }
}