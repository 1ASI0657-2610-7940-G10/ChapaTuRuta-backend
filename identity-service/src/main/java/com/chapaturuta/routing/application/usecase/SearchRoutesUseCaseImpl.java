package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchRoutesUseCaseImpl implements SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    public SearchRoutesUseCaseImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<RouteResponse> searchAvailableRoutes(String origin, String destination) {
        return routeRepository.findRoutes(origin, destination)
                .stream()
                .map(route -> new RouteResponse(
                        route.getId(),
                        route.getOriginDistrict(),
                        route.getDestinationDistrict(),
                        route.getPrice(),
                        route.getDurationMin()
                ))
                .collect(Collectors.toList());
    }
}