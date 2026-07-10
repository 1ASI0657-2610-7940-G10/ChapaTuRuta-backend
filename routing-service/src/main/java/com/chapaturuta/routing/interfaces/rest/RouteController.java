package com.chapaturuta.routing.interfaces.rest;

import com.chapaturuta.routing.application.dto.RouteRequest;
import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.dto.TripOptionResponse;
import com.chapaturuta.routing.application.usecase.ManageRouteUseCase;
import com.chapaturuta.routing.application.usecase.SearchRoutesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Búsqueda y Gestión de Rutas de Transporte")
public class RouteController {

    private final SearchRoutesUseCase searchRoutesUseCase;
    private final ManageRouteUseCase manageRouteUseCase;

    @GetMapping("/search")
    @Operation(summary = "Buscar rutas", description = "Busca rutas directas o con transbordos")
    public ResponseEntity<List<TripOptionResponse>> searchRoutes(
            @RequestParam String origin,
            @RequestParam String destination
    ) {
        List<TripOptionResponse> trips = searchRoutesUseCase.searchAvailableRoutes(origin, destination);
        return ResponseEntity.ok(trips);
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar estado", description = "Health check del microservicio")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Routing Service is running on port 8081");
    }

    @GetMapping
    @Operation(summary = "Listar todas las rutas", description = "Devuelve el catálogo completo de rutas")
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(manageRouteUseCase.getAllRoutes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de ruta", description = "Devuelve una ruta específica con sus paraderos")
    public ResponseEntity<?> getRouteById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(manageRouteUseCase.getRouteById(id));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva ruta", description = "Agrega una nueva ruta al catálogo")
    public ResponseEntity<?> createRoute(@RequestBody RouteRequest request) {
        try {
            RouteResponse response = manageRouteUseCase.createRoute(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ruta", description = "Modifica los datos de una ruta existente (precio, tiempo, etc.)")
    public ResponseEntity<?> updateRoute(@PathVariable UUID id, @RequestBody RouteRequest request) {
        try {
            return ResponseEntity.ok(manageRouteUseCase.updateRoute(id, request));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ruta", description = "Borra físicamente una ruta del catálogo")
    public ResponseEntity<?> deleteRoute(@PathVariable UUID id) {
        try {
            manageRouteUseCase.deleteRoute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}