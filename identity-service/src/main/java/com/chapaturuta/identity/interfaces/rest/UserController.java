package com.chapaturuta.identity.interfaces.rest;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.application.dto.UserRegistrationRequest;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.dto.UserUpdateRequest;
import com.chapaturuta.identity.application.usecase.AuthenticateUserUseCase;
import com.chapaturuta.identity.application.usecase.RegisterUserUseCase;
import com.chapaturuta.identity.application.usecase.ManageUserUseCase; // IMPORT FALTANTE
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints para registro y autenticación de usuarios")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final ManageUserUseCase manageUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          ManageUserUseCase manageUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.manageUserUseCase = manageUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un nuevo pasajero o conductor")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
        try {
            UserResponse response = registerUserUseCase.registerUser(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Debug Error: " + e.getMessage() + "\nCause: " + (e.getCause() != null ? e.getCause().getMessage() : "none") + "\nStack: " + stackTrace);
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

    @GetMapping("/profile/{id}")
    @Operation(summary = "Obtener Perfil", description = "Devuelve los datos del usuario")
    public ResponseEntity<?> getProfile(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(manageUserUseCase.getUserProfile(id));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/profile/{id}")
    @Operation(summary = "Actualizar Perfil", description = "Actualiza nombre o contraseña del usuario")
    public ResponseEntity<?> updateProfile(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.ok(manageUserUseCase.updateUserProfile(id, request));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/profile/{id}")
    @Operation(summary = "Eliminar Cuenta", description = "Borra físicamente al usuario de la plataforma")
    public ResponseEntity<?> deleteProfile(@PathVariable UUID id) {
        try {
            manageUserUseCase.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}