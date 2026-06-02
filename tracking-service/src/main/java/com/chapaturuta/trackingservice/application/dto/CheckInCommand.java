package com.chapaturuta.trackingservice.application.dto;

import java.util.UUID;

public record CheckInCommand(
        UUID driverId,
        UUID routeId,
        UUID stopId,
        Double latitude,
        Double longitude,
        Long timestamp
) {}