package com.chapaturuta.identity.application.dto;

import com.chapaturuta.identity.domain.model.Role;

public record UserRegistrationRequest(
        String name,
        String email,
        String password,
        Role role
) {}