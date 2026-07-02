package com.chapaturuta.routing.application.dto;

import java.util.List;

public record TripOptionResponse(
        List<RouteResponse> legs,
        Double totalPrice,
        Integer totalEstimatedDuration
) {}