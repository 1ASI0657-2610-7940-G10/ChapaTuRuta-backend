package com.chapaturuta.identity.interfaces.rest;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.usecase.AuthenticateUserUseCase;
import com.chapaturuta.identity.application.usecase.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints para registro y autenticación de usuarios")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo pasajero o conductor")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            UserResponse response = registerUserUseCase.registerUser(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar Sesión", description = "Valida credenciales y devuelve un JWT")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            String token = authenticateUserUseCase.authenticate(request);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}