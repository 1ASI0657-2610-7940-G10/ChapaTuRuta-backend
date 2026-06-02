package com.chapaturuta.trackingservice.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demand")
@Tag(name = "Demand Management", description = "Gestión de la demanda de pasajeros en paraderos")
public class DemandController {

    private final StringRedisTemplate redisTemplate;

    public DemandController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/join")
    @Operation(summary = "Pasajero elige ruta", description = "Registra al pasajero en espera en un paradero específico")
    public ResponseEntity<String> joinQueue(
            @RequestParam UUID routeId,
            @RequestParam UUID stopId,
            @RequestParam UUID passengerId) {

        // Llave estructurada para búsquedas eficientes
        String key = "route:" + routeId + ":stop:" + stopId + ":passenger:" + passengerId;

        // Le asignamos 1 hora de TTL por si el bus demora por tráfico
        redisTemplate.opsForValue().set(key, "waiting", Duration.ofHours(1));

        return ResponseEntity.ok("Pasajero registrado en espera exitosamente");
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "Ver demanda de la ruta", description = "Devuelve cuántos pasajeros esperan en cada paradero de la ruta")
    public ResponseEntity<Map<String, Integer>> getRouteDemand(@PathVariable UUID routeId) {
        // Buscamos todas las llaves de pasajeros esperando en cualquier paradero de esta ruta
        String pattern = "route:" + routeId + ":stop:*:passenger:*";
        Set<String> keys = redisTemplate.keys(pattern);

        Map<String, Integer> demandPerStop = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                // El formato de la llave es: route:{id}:stop:{id}:passenger:{id}
                String[] parts = key.split(":");
                if (parts.length >= 6) {
                    String stopId = parts[3];
                    // Sumamos +1 a la cuenta de ese paradero
                    demandPerStop.put(stopId, demandPerStop.getOrDefault(stopId, 0) + 1);
                }
            }
        }

        return ResponseEntity.ok(demandPerStop); // Devuelve ej. {"stop-uuid-1": 5, "stop-uuid-2": 2}
    }

    @PostMapping("/transfer")
    @Operation(summary = "Registrar transbordo", description = "Encola al pasajero en el siguiente tramo")
    public ResponseEntity<String> registerTransfer(
            @RequestParam UUID nextRouteId,
            @RequestParam UUID nextStopId,
            @RequestParam UUID passengerId) {

        String nextKey = "route:" + nextRouteId + ":stop:" + nextStopId + ":passenger:" + passengerId;
        redisTemplate.opsForValue().set(nextKey, "waiting_transfer", Duration.ofHours(1));

        return ResponseEntity.ok("Pasajero registrado para transbordo exitosamente");
    }
}