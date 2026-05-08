package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;

public interface RegisterUserUseCase {
    UserResponse registerUser(UserRegistrationRequest request);
}