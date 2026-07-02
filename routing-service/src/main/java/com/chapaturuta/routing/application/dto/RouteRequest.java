package com.chapaturuta.routing.application.dto;

public record RouteRequest(
        String originDistrict,
        String destinationDistrict,
        Double price,
        Integer durationMin,
        java.util.List<StopDTO> stops
) {}