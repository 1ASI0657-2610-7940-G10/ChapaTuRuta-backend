package com.chapaturuta.identity.infrastructure.persistence;

import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

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
                .createdAt(user.getCreatedAt())
                .build();

        UserEntity savedEntity = repository.save(entity);
        user.setId(savedEntity.getId());
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(entity ->
                User.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .email(entity.getEmail())
                        .password(entity.getPassword())
                        .role(entity.getRole())
                        .createdAt(entity.getCreatedAt())
                        .build()
        );
    }
}