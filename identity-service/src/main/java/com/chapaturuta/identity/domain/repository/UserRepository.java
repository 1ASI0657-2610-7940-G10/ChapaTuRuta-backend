package com.chapaturuta.identity.domain.repository;

import com.chapaturuta.identity.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
}