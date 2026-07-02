package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class TrackingQueryService {

    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ¡Alineado con tu application.yml!
    @Value("${mapbox.api-key}")
    private String mapboxApiKey;

    public TrackingQueryService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public EtaQueryResponse getRouteEta(UUID routeId, Double pasajeroLat, Double pasajeroLng) {
        String key = "route:" + routeId + ":location";
        String locationStr = redisTemplate.opsForValue().get(key);

        if (locationStr == null) {
            throw new IllegalArgumentException("No hay vehículos de esta ruta emitiendo señal actualmente.");
        }

        String[] parts = locationStr.split(",");
        Double busLat = Double.parseDouble(parts[0]);
        Double busLng = Double.parseDouble(parts[1]);

        String mapboxUrl = String.format(
                "https://api.mapbox.com/directions/v5/mapbox/driving/%s,%s;%s,%s?access_token=%s",
                busLng, busLat, pasajeroLng, pasajeroLat, mapboxApiKey
        );

        try {
            String responseJson = restTemplate.getForObject(mapboxUrl, String.class);
            JsonNode rootNode = objectMapper.readTree(responseJson);

            JsonNode routes = rootNode.path("routes");
            if (routes.isArray() && !routes.isEmpty()) {
                double durationSeconds = routes.get(0).path("duration").asDouble();

                int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
                String etaFormateado = durationMinutes + " min";

                return new EtaQueryResponse(routeId, busLat, busLng, etaFormateado);
            } else {
                return new EtaQueryResponse(routeId, busLat, busLng, "Calculando ruta...");
            }

        } catch (Exception e) {
            System.err.println("Error al consultar la API de Mapbox: " + e.getMessage());
            return new EtaQueryResponse(routeId, busLat, busLng, "ETA no disponible");
        }
    }
}