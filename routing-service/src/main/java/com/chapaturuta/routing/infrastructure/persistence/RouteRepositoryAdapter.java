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
        return Route.builder()
                .id(entity.getId())
                .originDistrict(entity.getOriginDistrict())
                .destinationDistrict(entity.getDestinationDistrict())
                .price(entity.getPrice())
                .durationMin(entity.getDurationMin())
                .build();
    }

    private RouteEntity toEntity(Route route) {
        return RouteEntity.builder()
                .id(route.getId())
                .originDistrict(route.getOriginDistrict())
                .destinationDistrict(route.getDestinationDistrict())
                .price(route.getPrice())
                .durationMin(route.getDurationMin())
                .build();
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