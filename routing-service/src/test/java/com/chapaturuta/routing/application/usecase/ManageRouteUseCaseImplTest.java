package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteRequest;
import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManageRouteUseCaseImpl - Tests")
class ManageRouteUseCaseImplTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private ManageRouteUseCaseImpl manageRouteUseCase;

    private RouteRequest validRequest;
    private Route mockRoute;
    private UUID routeId;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        validRequest = new RouteRequest("Ate", "Cercado de Lima", 3.50, 45);
        mockRoute = Route.builder()
                .id(routeId)
                .originDistrict("Ate")
                .destinationDistrict("Cercado de Lima")
                .price(3.50)
                .durationMin(45)
                .build();
    }

    @Nested
    @DisplayName("Crear rutas")
    class CreateRouteTests {

        @Test
        @DisplayName("Debe crear una nueva ruta exitosamente")
        void createRoute_Successful_ReturnsRouteResponse() {
            when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

            RouteResponse response = manageRouteUseCase.createRoute(validRequest);

            assertNotNull(response);
            assertEquals(routeId, response.routeId());
            assertEquals("Ate", response.origin());
            assertEquals("Cercado de Lima", response.destination());
            assertEquals(3.50, response.price());
            assertEquals(45, response.estimatedDuration());
            verify(routeRepository).save(any(Route.class));
        }

        @Test
        @DisplayName("Debe guardar los datos exactos de la solicitud")
        void createRoute_SavesCorrectData() {
            when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

            manageRouteUseCase.createRoute(validRequest);

            verify(routeRepository).save(argThat(route ->
                    route.getOriginDistrict().equals("Ate") &&
                    route.getDestinationDistrict().equals("Cercado de Lima") &&
                    route.getPrice().equals(3.50) &&
                    route.getDurationMin().equals(45)
            ));
        }

        @Test
        @DisplayName("Debe asignar UUID a la nueva ruta")
        void createRoute_AssignsUUID() {
            when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

            RouteResponse response = manageRouteUseCase.createRoute(validRequest);

            assertNotNull(response.routeId());
        }
    }

    @Nested
    @DisplayName("Actualizar rutas")
    class UpdateRouteTests {

        @Test
        @DisplayName("Debe actualizar una ruta existente")
        void updateRoute_Successful_ReturnsUpdatedRoute() {
            Route updatedRoute = Route.builder()
                    .id(routeId)
                    .originDistrict("San Isidro")
                    .destinationDistrict("Miraflores")
                    .price(4.50)
                    .durationMin(30)
                    .build();

            RouteRequest updateRequest = new RouteRequest("San Isidro", "Miraflores", 4.50, 30);

            when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));
            when(routeRepository.save(any(Route.class))).thenReturn(updatedRoute);

            RouteResponse response = manageRouteUseCase.updateRoute(routeId, updateRequest);

            assertNotNull(response);
            assertEquals("San Isidro", response.origin());
            assertEquals("Miraflores", response.destination());
            assertEquals(4.50, response.price());
            assertEquals(30, response.estimatedDuration());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la ruta no existe")
        void updateRoute_RouteNotFound_ThrowsException() {
            when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    manageRouteUseCase.updateRoute(routeId, validRequest));
        }

        @Test
        @DisplayName("Debe verificar que la ruta existe antes de actualizar")
        void updateRoute_VerifiesExistence() {
            when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));
            when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

            manageRouteUseCase.updateRoute(routeId, validRequest);

            verify(routeRepository).findById(routeId);
        }
    }

    @Nested
    @DisplayName("Eliminar rutas")
    class DeleteRouteTests {

        @Test
        @DisplayName("Debe eliminar una ruta existente")
        void deleteRoute_Successful_DeletesRoute() {
            when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));

            manageRouteUseCase.deleteRoute(routeId);

            verify(routeRepository).deleteById(routeId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando intenta eliminar ruta inexistente")
        void deleteRoute_RouteNotFound_ThrowsException() {
            when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                    manageRouteUseCase.deleteRoute(routeId));
        }

        @Test
        @DisplayName("No debe eliminar si la ruta no existe")
        void deleteRoute_DoesNotDeleteIfNotFound() {
            when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

            try {
                manageRouteUseCase.deleteRoute(routeId);
            } catch (IllegalArgumentException e) {
                // Expected
            }

            verify(routeRepository, never()).deleteById(routeId);
        }
    }

    @Nested
    @DisplayName("Obtener rutas")
    class GetAllRoutesTests {

        @Test
        @DisplayName("Debe retornar todas las rutas")
        void getAllRoutes_ReturnsAllRoutes() {
            Route route2 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("Miraflores")
                    .destinationDistrict("San Isidro")
                    .price(2.50)
                    .durationMin(20)
                    .build();

            when(routeRepository.findAll()).thenReturn(List.of(mockRoute, route2));

            List<RouteResponse> responses = manageRouteUseCase.getAllRoutes();

            assertNotNull(responses);
            assertEquals(2, responses.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay rutas")
        void getAllRoutes_NoRoutes_ReturnsEmptyList() {
            when(routeRepository.findAll()).thenReturn(Collections.emptyList());

            List<RouteResponse> responses = manageRouteUseCase.getAllRoutes();

            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("Debe mapear correctamente todas las rutas")
        void getAllRoutes_MapsCorrectly() {
            when(routeRepository.findAll()).thenReturn(List.of(mockRoute));

            List<RouteResponse> responses = manageRouteUseCase.getAllRoutes();

            assertEquals(1, responses.size());
            RouteResponse response = responses.get(0);
            assertEquals(mockRoute.getId(), response.routeId());
            assertEquals(mockRoute.getOriginDistrict(), response.origin());
            assertEquals(mockRoute.getDestinationDistrict(), response.destination());
            assertEquals(mockRoute.getPrice(), response.price());
            assertEquals(mockRoute.getDurationMin(), response.estimatedDuration());
        }
    }
}

