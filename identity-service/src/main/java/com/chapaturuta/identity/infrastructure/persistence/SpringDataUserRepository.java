package com.chapaturuta.identity.infrastructure.persistence;

import com.chapaturuta.identity.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByCompanyIdAndRole(UUID companyId, Role role);
}