package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class TrackingQueryService {

    private final StringRedisTemplate redisTemplate;
    private final String mapboxApiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TrackingQueryService(StringRedisTemplate redisTemplate,
                                @Value("${mapbox.api-key}") String mapboxApiKey) {
        this.redisTemplate = redisTemplate;
        this.mapboxApiKey = mapboxApiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public EtaQueryResponse getRouteEta(UUID routeId, Double pasajeroLat, Double pasajeroLng) {
        String key = "route:" + routeId + ":location";
        String location = redisTemplate.opsForValue().get(key);

        if (location == null) {
            throw new IllegalArgumentException("No hay ubicación activa para esta ruta");
        }

        String[] coords = location.split(",");
        Double busLat = Double.parseDouble(coords[0]);
        Double busLng = Double.parseDouble(coords[1]);

        String estimatedTime = "Calculando...";

        try {
            String coordenadas = busLng + "," + busLat + ";" + pasajeroLng + "," + pasajeroLat;

            String url = "https://api.mapbox.com/directions-matrix/v1/mapbox/driving/"
                    + coordenadas + "?access_token=" + mapboxApiKey;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            if ("Ok".equals(root.path("code").asText())) {
                double durationSeconds = root.path("durations").get(0).get(1).asDouble();

                int durationMinutes = (int) Math.round(durationSeconds / 60.0);
                estimatedTime = durationMinutes + " mins";
            }

        } catch (Exception e) {
            System.err.println("Error conectando con Mapbox: " + e.getMessage());
            estimatedTime = "ETA no disponible";
        }

        return new EtaQueryResponse(routeId, busLat, busLng, estimatedTime);
    }
}