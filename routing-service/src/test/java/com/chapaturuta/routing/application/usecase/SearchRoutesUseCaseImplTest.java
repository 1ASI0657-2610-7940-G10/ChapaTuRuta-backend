package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchRoutesUseCaseImplTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private SearchRoutesUseCaseImpl searchRoutesUseCase;

    private Route mockRoute;
    private UUID expectedRouteId;

    @BeforeEach
    void setUp() {
        expectedRouteId = UUID.randomUUID();
        mockRoute = Route.builder()
                .id(expectedRouteId)
                .originDistrict("Ate")
                .destinationDistrict("Cercado de Lima")
                .price(3.50)
                .durationMin(45)
                .build();
    }

    @Test
    void searchAvailableRoutes_Successful_ReturnsRouteResponseList() {
        when(routeRepository.findRoutes("Ate", "Cercado de Lima"))
                .thenReturn(List.of(mockRoute));

        List<RouteResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado de Lima");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(expectedRouteId, responses.get(0).routeId());
        assertEquals("Ate", responses.get(0).origin());
        assertEquals("Cercado de Lima", responses.get(0).destination());
        assertEquals(3.50, responses.get(0).price());
        assertEquals(45, responses.get(0).estimatedDuration());

        verify(routeRepository, times(1)).findRoutes("Ate", "Cercado de Lima");
    }

    @Test
    void searchAvailableRoutes_NoResults_ReturnsEmptyList() {
        when(routeRepository.findRoutes("Surco", "Callao"))
                .thenReturn(Collections.emptyList());

        List<RouteResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Surco", "Callao");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(routeRepository, times(1)).findRoutes("Surco", "Callao");
    }
}