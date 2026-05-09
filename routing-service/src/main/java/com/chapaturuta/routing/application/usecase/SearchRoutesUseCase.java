package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteResponse;

import java.util.List;

public interface SearchRoutesUseCase {
    List<RouteResponse> searchAvailableRoutes(String origin, String destination);
}