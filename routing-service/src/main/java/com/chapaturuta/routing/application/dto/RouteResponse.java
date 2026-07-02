package com.chapaturuta.routing.application.dto;

import java.util.UUID;

public record RouteResponse(
        UUID routeId,
        String origin,
        String destination,
        Double price,
        Integer estimatedDuration,
        java.util.List<StopDTO> stops
) {}