package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.dto.UserUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface ManageUserUseCase {
    UserResponse getUserProfile(UUID userId);
    UserResponse updateUserProfile(UUID userId, UserUpdateRequest request);
    void deleteUser(UUID userId);
    List<UserResponse> getDriversByCompany(UUID companyId);
}