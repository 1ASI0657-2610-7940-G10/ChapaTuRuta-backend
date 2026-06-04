package com.chapaturuta.identity.interfaces.rest;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.usecase.AuthenticateUserUseCase;
import com.chapaturuta.identity.application.usecase.ManageUserUseCase;
import com.chapaturuta.identity.application.usecase.RegisterUserUseCase;
import com.chapaturuta.identity.domain.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private ManageUserUseCase manageUserUseCase;

    @Test
    void registerUser_Returns201Created() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("Ana", "ana@mail.com", "pass", Role.PASSENGER, null);
        UserResponse response = new UserResponse(UUID.randomUUID(), "Ana", "ana@mail.com", Role.PASSENGER, LocalDateTime.now());

        when(registerUserUseCase.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@mail.com"));
    }

    @Test
    void registerUser_Returns400BadRequest_WhenExceptionThrown() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest("Ana", "ana@mail.com", "pass", Role.PASSENGER, null);

        when(registerUserUseCase.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("El correo ya está registrado"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El correo ya está registrado"));
    }

    @Test
    void login_Returns200OkWithToken() throws Exception {
        LoginRequest request = new LoginRequest("ana@mail.com", "pass");

        when(authenticateUserUseCase.authenticate(any(LoginRequest.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("fake-jwt-token"));
    }

    @Test
    void login_Returns401Unauthorized_WhenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("ana@mail.com", "wrongpass");

        when(authenticateUserUseCase.authenticate(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Credenciales inválidas"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));
    }
}