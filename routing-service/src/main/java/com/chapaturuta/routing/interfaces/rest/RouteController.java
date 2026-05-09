package com.chapaturuta.routing.interfaces.rest;

import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.usecase.SearchRoutesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final SearchRoutesUseCase searchRoutesUseCase;

    @GetMapping("/search")
    public ResponseEntity<List<RouteResponse>> searchRoutes(
            @RequestParam String origin,
            @RequestParam String destination
    ) {
        List<RouteResponse> routes = searchRoutesUseCase.searchAvailableRoutes(origin, destination);
        return ResponseEntity.ok(routes);
    }

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Routing Service is running on port 8081");
    }
}