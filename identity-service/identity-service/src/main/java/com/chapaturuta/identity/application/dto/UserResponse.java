package com.chapaturuta.identity.application.dto;

import com.chapaturuta.identity.domain.model.Role;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {}