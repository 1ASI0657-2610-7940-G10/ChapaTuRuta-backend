package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.LoginRequest;

public interface AuthenticateUserUseCase {
    String authenticate(LoginRequest request);
}