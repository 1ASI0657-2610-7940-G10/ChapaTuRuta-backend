package com.chapaturuta.trackingservice.interfaces.rest;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.application.dto.CheckInRequest;
import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import com.chapaturuta.trackingservice.application.usecase.TrackingCommandService;
import com.chapaturuta.trackingservice.application.usecase.TrackingQueryService;
import com.chapaturuta.trackingservice.domain.valueobject.CoordenadasGPS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
@Tag(name = "Tracking & ETA", description = "Endpoints CQRS para ubicación de vehículos")
public class TrackingController {

    private final TrackingCommandService commandService;
    private final TrackingQueryService queryService;

    public TrackingController(TrackingCommandService commandService, TrackingQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/check-in")
    @Operation(summary = "Registrar Check-in del Conductor", description = "Guarda ubicación en Redis y dispara evento RabbitMQ")
    public ResponseEntity<String> registerCheckIn(@RequestBody CheckInRequest request) {
        try {
            CoordenadasGPS gps = new CoordenadasGPS(request.latitude(), request.longitude());

            CheckInCommand command = new CheckInCommand(
                    request.driverId(),
                    request.routeId(),
                    request.stopId(),
                    gps,
                    request.timestamp()
            );

            commandService.processCheckIn(command);
            return new ResponseEntity<>("Check-in procesado asíncronamente", HttpStatus.ACCEPTED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error de validación: " + e.getMessage());
        }
    }

    @GetMapping("/eta/{routeId}")
    @Operation(summary = "Consultar ETA de una ruta", description = "Lectura ultrarrápida desde Redis y cálculo de ETA real con Google Maps")
    public ResponseEntity<EtaQueryResponse> getEta(
            @PathVariable UUID routeId,
            @RequestParam Double pasajeroLat,
            @RequestParam Double pasajeroLng) {
        try {
            EtaQueryResponse response = queryService.getRouteEta(routeId, pasajeroLat, pasajeroLng);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}