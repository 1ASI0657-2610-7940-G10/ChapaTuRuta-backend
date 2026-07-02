package com.chapaturuta.identity.infrastructure.persistence;

import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    public UserRepositoryAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .routeId(user.getRouteId())
                .createdAt(user.getCreatedAt())
                .build();

        UserEntity savedEntity = repository.save(entity);
        user.setId(savedEntity.getId());
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toModel);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toModel);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<User> findByCompanyIdAndRole(UUID companyId, Role role) {
        return repository.findByCompanyIdAndRole(companyId, role).stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public void deleteByCompanyId(UUID companyId) {
        repository.deleteByCompanyId(companyId);
    }

    private User toModel(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole())
                .companyId(entity.getCompanyId())
                .routeId(entity.getRouteId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}