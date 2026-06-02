package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteRequest;
import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface ManageRouteUseCase {
    RouteResponse createRoute(RouteRequest request);
    RouteResponse updateRoute(UUID id, RouteRequest request);
    void deleteRoute(UUID id);
    List<RouteResponse> getAllRoutes();
}

@Service
class ManageRouteUseCaseImpl implements ManageRouteUseCase {

    private final RouteRepository routeRepository;

    public ManageRouteUseCaseImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public RouteResponse createRoute(RouteRequest request) {
        Route newRoute = Route.builder()
                .id(UUID.randomUUID())
                .originDistrict(request.originDistrict())
                .destinationDistrict(request.destinationDistrict())
                .price(request.price())
                .durationMin(request.durationMin())
                .build();

        Route savedRoute = routeRepository.save(newRoute);
        return mapToResponse(savedRoute);
    }

    @Override
    public RouteResponse updateRoute(UUID id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada"));

        route.setOriginDistrict(request.originDistrict());
        route.setDestinationDistrict(request.destinationDistrict());
        route.setPrice(request.price());
        route.setDurationMin(request.durationMin());

        Route updatedRoute = routeRepository.save(route);
        return mapToResponse(updatedRoute);
    }

    @Override
    public void deleteRoute(UUID id) {
        if (routeRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Ruta no encontrada");
        }
        routeRepository.deleteById(id);
    }

    @Override
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RouteResponse mapToResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getOriginDistrict(),
                route.getDestinationDistrict(),
                route.getPrice(),
                route.getDurationMin()
        );
    }
}