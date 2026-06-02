package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.dto.TripOptionResponse;
import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchRoutesUseCaseImpl implements SearchRoutesUseCase {

    private final RouteRepository routeRepository;

    public SearchRoutesUseCaseImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<TripOptionResponse> searchAvailableRoutes(String origin, String destination) {
        List<TripOptionResponse> tripOptions = new ArrayList<>();

        List<Route> directRoutes = routeRepository.findRoutes(origin, destination);
        for (Route route : directRoutes) {
            RouteResponse rr = new RouteResponse(route.getId(), route.getOriginDistrict(), route.getDestinationDistrict(), route.getPrice(), route.getDurationMin());
            tripOptions.add(new TripOptionResponse(List.of(rr), rr.price(), rr.estimatedDuration()));
        }

        List<Route> startingRoutes = routeRepository.findByOrigin(origin);
        List<Route> endingRoutes = routeRepository.findByDestination(destination);

        for (Route leg1 : startingRoutes) {
            for (Route leg2 : endingRoutes) {
                if (leg1.getDestinationDistrict().equalsIgnoreCase(leg2.getOriginDistrict())) {
                    RouteResponse rr1 = new RouteResponse(leg1.getId(), leg1.getOriginDistrict(), leg1.getDestinationDistrict(), leg1.getPrice(), leg1.getDurationMin());
                    RouteResponse rr2 = new RouteResponse(leg2.getId(), leg2.getOriginDistrict(), leg2.getDestinationDistrict(), leg2.getPrice(), leg2.getDurationMin());

                    Double totalPrice = leg1.getPrice() + leg2.getPrice();
                    Integer totalDuration = leg1.getDurationMin() + leg2.getDurationMin() + 5;

                    tripOptions.add(new TripOptionResponse(List.of(rr1, rr2), totalPrice, totalDuration));
                }
            }
        }

        return tripOptions;
    }
}