package com.chapaturuta.routing.infrastructure.persistence;

import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import com.chapaturuta.routing.infrastructure.SpringDataRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RouteRepositoryAdapter implements RouteRepository {

    private final SpringDataRouteRepository springDataRouteRepository;

    @Override
    public List<Route> findRoutes(String origin, String destination) {
        return springDataRouteRepository
                .findByOriginDistrictIgnoreCaseAndDestinationDistrictIgnoreCase(origin, destination)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Route save(Route route) {
        RouteEntity entity = toEntity(route);
        RouteEntity saved = springDataRouteRepository.save(entity);
        return toModel(saved);
    }

    private Route toModel(RouteEntity entity) {
        Route route = Route.builder()
                .id(entity.getId())
                .originDistrict(entity.getOriginDistrict())
                .destinationDistrict(entity.getDestinationDistrict())
                .price(entity.getPrice())
                .durationMin(entity.getDurationMin())
                .build();

        if (entity.getStops() != null) {
            route.setStops(entity.getStops().stream().map(s -> com.chapaturuta.routing.domain.model.RouteStop.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .latitude(s.getLatitude())
                    .longitude(s.getLongitude())
                    .address(s.getAddress())
                    .stopOrder(s.getStopOrder())
                    .build()).collect(Collectors.toList()));
        }

        return route;
    }

    private RouteEntity toEntity(Route route) {
        RouteEntity entity = RouteEntity.builder()
                .id(route.getId())
                .originDistrict(route.getOriginDistrict())
                .destinationDistrict(route.getDestinationDistrict())
                .price(route.getPrice())
                .durationMin(route.getDurationMin())
                .build();

        if (route.getStops() != null) {
            entity.setStops(route.getStops().stream().map(s -> com.chapaturuta.routing.infrastructure.persistence.RouteStopEntity.builder()
                    .id(s.getId())
                    .name(s.getName())
                    .latitude(s.getLatitude())
                    .longitude(s.getLongitude())
                    .address(s.getAddress())
                    .stopOrder(s.getStopOrder())
                    .route(entity)
                    .build()).collect(Collectors.toList()));
        }

        return entity;
    }

    @Override
    public List<Route> findByOrigin(String origin) {
        return springDataRouteRepository.findByOriginDistrictIgnoreCase(origin)
                .stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public List<Route> findByDestination(String destination) {
        return springDataRouteRepository.findByDestinationDistrictIgnoreCase(destination)
                .stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<Route> findById(UUID id) {
        return springDataRouteRepository.findById(id).map(this::toModel);
    }

    @Override
    public List<Route> findAll() {
        return springDataRouteRepository.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springDataRouteRepository.deleteById(id);
    }
}