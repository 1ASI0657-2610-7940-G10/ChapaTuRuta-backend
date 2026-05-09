package com.chapaturuta.trackingservice.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demand")
@Tag(name = "Demanda y Concurrencia", description = "Gestión de pasajeros esperando en paraderos")
public class DemandController {

    private final StringRedisTemplate redisTemplate;

    public DemandController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/waiting")
    @Operation(summary = "Registrar pasajero en espera", description = "Añade al pasajero a la lista del paradero con un Timeout de 2 minutos")
    public ResponseEntity<String> registerWaitingPassenger(
            @RequestParam UUID stopId,
            @RequestParam UUID passengerId) {

        String key = "stop:" + stopId + ":passenger:" + passengerId;

        // El pasajero se guarda en Redis y se ELIMINA AUTOMÁTICAMENTE a los 2 minutos (Timeout exigido)
        redisTemplate.opsForValue().set(key, "waiting", Duration.ofMinutes(2));

        return ResponseEntity.ok("Pasajero registrado en la cola de espera del paradero");
    }

    @PostMapping("/board")
    @Operation(summary = "Confirmar abordaje", description = "El pasajero confirma que subió al bus y se le quita de la lista de espera")
    public ResponseEntity<String> confirmBoarding(
            @RequestParam UUID stopId,
            @RequestParam UUID passengerId) {

        String key = "stop:" + stopId + ":passenger:" + passengerId;

        // Verificamos si aún está en la lista (si no pasaron los 2 minutos)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.delete(key); // Lo quitamos porque ya subió
            return ResponseEntity.ok("Abordaje confirmado exitosamente");
        } else {
            return ResponseEntity.badRequest().body("El pasajero ya no está en la lista de espera (Timeout de 2 mins alcanzado)");
        }
    }

    @GetMapping("/count/{stopId}")
    @Operation(summary = "Obtener nivel de concurrencia", description = "Retorna cuántas personas están esperando en un paradero")
    public ResponseEntity<Integer> getDemandCount(@PathVariable UUID stopId) {
        // Busca en Redis cuántos pasajeros tienen llaves activas en ese paradero
        int count = redisTemplate.keys("stop:" + stopId + ":passenger:*").size();
        return ResponseEntity.ok(count);
    }
}