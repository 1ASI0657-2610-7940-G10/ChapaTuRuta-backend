package com.chapaturuta.trackingservice.application.dto;

import com.chapaturuta.trackingservice.domain.valueobject.CoordenadasGPS;
import java.util.UUID;

public record CheckInCommand(
        UUID driverId,
        UUID routeId,
        UUID stopId,
        CoordenadasGPS coordenadas,
        Long timestamp
) {}