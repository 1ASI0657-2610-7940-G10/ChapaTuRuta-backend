package com.chapaturuta.routing.application.dto;

import java.util.UUID;

public record StopDTO(
        UUID id,
        String name,
        Double latitude,
        Double longitude,
        String address,
        Integer stopOrder
) {}
