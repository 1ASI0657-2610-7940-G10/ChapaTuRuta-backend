package com.chapaturuta.trackingservice.application.dto;

import java.util.UUID;

public record CheckInCommand(
        UUID driverId,
        UUID routeId,
        Double latitude,
        Double longitude
) {}