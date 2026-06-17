package com.chapaturuta.identity.interfaces.rest;

import com.chapaturuta.identity.application.dto.CompanyRegistrationRequest;
import com.chapaturuta.identity.application.dto.CompanyResponse;
import com.chapaturuta.identity.application.dto.UserResponse;
import com.chapaturuta.identity.application.usecase.RegisterCompanyUseCase;
import com.chapaturuta.identity.application.usecase.ManageUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@Tag(name = "Companies", description = "Gestión de Empresas de Transporte")
public class CompanyController {

    private final RegisterCompanyUseCase registerCompanyUseCase;
    private final ManageUserUseCase manageUserUseCase; // VARIABLE FALTANTE

    // CONSTRUCTOR CORREGIDO
    public CompanyController(RegisterCompanyUseCase registerCompanyUseCase, ManageUserUseCase manageUserUseCase) {
        this.registerCompanyUseCase = registerCompanyUseCase;
        this.manageUserUseCase = manageUserUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar una nueva Empresa", description = "Requiere que el ID del manager pertenezca a un usuario con rol MANAGER")
    public ResponseEntity<?> registerCompany(@RequestBody CompanyRegistrationRequest request) {
        try {
            CompanyResponse response = registerCompanyUseCase.registerCompany(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{companyId}/drivers")
    @Operation(summary = "Listar choferes", description = "Devuelve todos los conductores vinculados a una empresa")
    public ResponseEntity<List<UserResponse>> getCompanyDrivers(@PathVariable UUID companyId) {
        List<UserResponse> drivers = manageUserUseCase.getDriversByCompany(companyId);
        return ResponseEntity.ok(drivers);
    }
}