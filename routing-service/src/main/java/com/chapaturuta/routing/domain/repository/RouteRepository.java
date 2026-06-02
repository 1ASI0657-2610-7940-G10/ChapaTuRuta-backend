package com.chapaturuta.routing.domain.repository;

import com.chapaturuta.routing.domain.model.Route;
import java.util.List;

public interface RouteRepository {
    List<Route> findRoutes(String origin, String destination);
    List<Route> findByOrigin(String origin);
    List<Route> findByDestination(String destination);
    Route save(Route route);
}