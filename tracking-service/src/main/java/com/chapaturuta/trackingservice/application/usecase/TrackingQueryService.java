package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TrackingQueryService {

    private final StringRedisTemplate redisTemplate;

    public TrackingQueryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public EtaQueryResponse getRouteEta(UUID routeId) {
        String key = "route:" + routeId + ":location";
        String location = redisTemplate.opsForValue().get(key);

        if (location == null) {
            throw new IllegalArgumentException("No hay ubicación activa para esta ruta");
        }

        String[] coords = location.split(",");

        // Simulación de cálculo de ETA O(1)
        return new EtaQueryResponse(
                routeId,
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                "5 minutos"
        );
    }
}