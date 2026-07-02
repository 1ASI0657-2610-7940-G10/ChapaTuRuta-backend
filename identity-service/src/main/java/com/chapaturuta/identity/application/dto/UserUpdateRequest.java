package com.chapaturuta.identity.application.dto;

public record UserUpdateRequest(
        String name,
        String password
) {}