package com.chapaturuta.identity.application.dto;
import com.chapaturuta.identity.domain.model.Role;
import java.util.UUID;

public record UserRegistrationRequest(
        String name,
        String email,
        String password,
        Role role,
        UUID companyId,
        UUID routeId
) {
    public UserRegistrationRequest(String name, String email, String password, Role role, UUID companyId) {
        this(name, email, password, role, companyId, null);
    }
}