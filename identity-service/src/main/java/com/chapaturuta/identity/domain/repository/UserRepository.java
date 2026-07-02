package com.chapaturuta.identity.domain.repository;

import com.chapaturuta.identity.domain.model.Role;
import com.chapaturuta.identity.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);

    void deleteById(UUID id);
    void deleteByCompanyId(UUID companyId);
    List<User> findByCompanyIdAndRole(UUID companyId, Role role);
}