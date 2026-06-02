package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.TripOptionResponse;
import java.util.List;

public interface SearchRoutesUseCase {
    List<TripOptionResponse> searchAvailableRoutes(String origin, String destination);
}