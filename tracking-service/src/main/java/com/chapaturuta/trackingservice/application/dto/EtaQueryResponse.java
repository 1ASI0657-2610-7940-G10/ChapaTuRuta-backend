package com.chapaturuta.trackingservice.application.dto;

import java.util.UUID;

public record EtaQueryResponse(
        UUID routeId,
        Double currentLatitude,
        Double currentLongitude,
        String estimatedTime
) {}