package com.chapaturuta.routing.application.usecase;

import com.chapaturuta.routing.application.dto.RouteResponse;
import com.chapaturuta.routing.application.dto.TripOptionResponse;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchRoutesUseCaseImpl - Tests")
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

    @Nested
    @DisplayName("Búsqueda de rutas directas")
    class DirectRoutesTests {

        @Test
        @DisplayName("Debe retornar una ruta cuando existe ruta directa")
        void searchAvailableRoutes_Successful_ReturnsRouteResponseList() {
            when(routeRepository.findRoutes("Ate", "Cercado de Lima")).thenReturn(List.of(mockRoute));
            when(routeRepository.findByOrigin("Ate")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Cercado de Lima")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado de Lima");

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(expectedRouteId, responses.get(0).legs().get(0).routeId());
            assertEquals("Ate", responses.get(0).legs().get(0).origin());
            assertEquals("Cercado de Lima", responses.get(0).legs().get(0).destination());
            assertEquals(3.50, responses.get(0).totalPrice());
            assertEquals(45, responses.get(0).totalEstimatedDuration());
            assertEquals(1, responses.get(0).legs().size());
        }

        @Test
        @DisplayName("Debe retornar múltiples rutas directas")
        void searchAvailableRoutes_MultipleDirectRoutes_ReturnsList() {
            UUID routeId2 = UUID.randomUUID();
            Route mockRoute2 = Route.builder()
                    .id(routeId2)
                    .originDistrict("Ate")
                    .destinationDistrict("Cercado de Lima")
                    .price(4.00)
                    .durationMin(50)
                    .build();

            when(routeRepository.findRoutes("Ate", "Cercado de Lima")).thenReturn(List.of(mockRoute, mockRoute2));
            when(routeRepository.findByOrigin("Ate")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Cercado de Lima")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado de Lima");

            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals(3.50, responses.get(0).totalPrice());
            assertEquals(4.00, responses.get(1).totalPrice());
        }

        @Test
        @DisplayName("Debe respetar mayúsculas y minúsculas en buscar rutas directas")
        void searchAvailableRoutes_CaseInsensitive_FindsRoutes() {
            when(routeRepository.findRoutes("ate", "cercado de lima")).thenReturn(List.of(mockRoute));
            when(routeRepository.findByOrigin("ate")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("cercado de lima")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("ate", "cercado de lima");

            assertNotNull(responses);
            assertEquals(1, responses.size());
            verify(routeRepository).findRoutes("ate", "cercado de lima");
        }
    }

    @Nested
    @DisplayName("Búsqueda con transbordos")
    class TransferRoutesTests {

        @Test
        @DisplayName("Debe encontrar ruta con un transbordo")
        void searchAvailableRoutes_WithTransfer_ReturnsTransferOption() {
            UUID leg1Id = UUID.randomUUID();
            UUID leg2Id = UUID.randomUUID();

            Route leg1 = Route.builder()
                    .id(leg1Id)
                    .originDistrict("Ate")
                    .destinationDistrict("La Victoria")
                    .price(3.00)
                    .durationMin(30)
                    .build();

            Route leg2 = Route.builder()
                    .id(leg2Id)
                    .originDistrict("La Victoria")
                    .destinationDistrict("Cercado de Lima")
                    .price(2.50)
                    .durationMin(20)
                    .build();

            when(routeRepository.findRoutes("Ate", "Cercado de Lima")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("Ate")).thenReturn(List.of(leg1));
            when(routeRepository.findByDestination("Cercado de Lima")).thenReturn(List.of(leg2));

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado de Lima");

            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals(2, responses.get(0).legs().size());
            assertEquals(5.50, responses.get(0).totalPrice());
            assertEquals(55, responses.get(0).totalEstimatedDuration()); // 30 + 20 + 5 min de espera
        }

        @Test
        @DisplayName("Debe incluir 5 minutos adicionales en transbordos")
        void searchAvailableRoutes_TransferIncludesWaitTime() {
            UUID leg1Id = UUID.randomUUID();
            UUID leg2Id = UUID.randomUUID();

            Route leg1 = Route.builder()
                    .id(leg1Id)
                    .originDistrict("San Juan")
                    .destinationDistrict("Puente")
                    .price(3.00)
                    .durationMin(30)
                    .build();

            Route leg2 = Route.builder()
                    .id(leg2Id)
                    .originDistrict("Puente")
                    .destinationDistrict("Centro")
                    .price(2.00)
                    .durationMin(15)
                    .build();

            when(routeRepository.findRoutes("San Juan", "Centro")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("San Juan")).thenReturn(List.of(leg1));
            when(routeRepository.findByDestination("Centro")).thenReturn(List.of(leg2));

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("San Juan", "Centro");

            assertTrue(responses.size() > 0);
            assertEquals(50, responses.get(0).totalEstimatedDuration()); // 30 + 15 + 5
        }

        @Test
        @DisplayName("No debe crear transbordo si destino intermedio no coincide")
        void searchAvailableRoutes_NoTransferIfNoMatch() {
            Route leg1 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("Ate")
                    .destinationDistrict("La Victoria")
                    .price(3.00)
                    .durationMin(30)
                    .build();

            Route leg2 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("San Borja")  // No coincide
                    .destinationDistrict("Cercado de Lima")
                    .price(2.50)
                    .durationMin(20)
                    .build();

            when(routeRepository.findRoutes("Ate", "Cercado de Lima")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("Ate")).thenReturn(List.of(leg1));
            when(routeRepository.findByDestination("Cercado de Lima")).thenReturn(List.of(leg2));

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado de Lima");

            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("Debe manejar múltiples opciones de transbordo")
        void searchAvailableRoutes_MultipleTransferOptions() {
            Route leg1_opt1 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("Ate")
                    .destinationDistrict("Centro")
                    .price(3.00)
                    .durationMin(30)
                    .build();

            Route leg1_opt2 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("Ate")
                    .destinationDistrict("Centro")
                    .price(3.50)
                    .durationMin(25)
                    .build();

            Route leg2 = Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict("Centro")
                    .destinationDistrict("Cercado")
                    .price(2.00)
                    .durationMin(20)
                    .build();

            when(routeRepository.findRoutes("Ate", "Cercado")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("Ate")).thenReturn(List.of(leg1_opt1, leg1_opt2));
            when(routeRepository.findByDestination("Cercado")).thenReturn(List.of(leg2));

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Cercado");

            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertTrue(responses.stream().allMatch(r -> r.legs().size() == 2));
        }
    }

    @Nested
    @DisplayName("Sin resultados")
    class NoResultsTests {

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay rutas")
        void searchAvailableRoutes_NoResults_ReturnsEmptyList() {
            when(routeRepository.findRoutes("Surco", "Callao")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("Surco")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Callao")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Surco", "Callao");
            
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("Debe retornar lista vacía para origen sin rutas salientes")
        void searchAvailableRoutes_NoOriginRoutes_ReturnsEmptyList() {
            when(routeRepository.findRoutes("Ancón", "Chosica")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("Ancón")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Chosica")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ancón", "Chosica");
            
            assertTrue(responses.isEmpty());
            verify(routeRepository).findRoutes("Ancón", "Chosica");
            verify(routeRepository).findByOrigin("Ancón");
            verify(routeRepository).findByDestination("Chosica");
        }
    }

    @Nested
    @DisplayName("Validaciones")
    class ValidationTests {

        @Test
        @DisplayName("Debe validar respuesta no nula")
        void searchAvailableRoutes_ResponseNotNull() {
            when(routeRepository.findRoutes("any", "any")).thenReturn(Collections.emptyList());
            when(routeRepository.findByOrigin("any")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("any")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("any", "any");
            
            assertNotNull(responses);
        }

        @Test
        @DisplayName("Debe validar precio total correcto")
        void searchAvailableRoutes_CorrectTotalPrice() {
            when(routeRepository.findRoutes("Ate", "Lima")).thenReturn(List.of(mockRoute));
            when(routeRepository.findByOrigin("Ate")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Lima")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Lima");
            
            assertEquals(3.50, responses.get(0).totalPrice());
            assertTrue(responses.get(0).totalPrice() > 0);
        }

        @Test
        @DisplayName("Debe validar duración total correcta")
        void searchAvailableRoutes_CorrectTotalDuration() {
            when(routeRepository.findRoutes("Ate", "Lima")).thenReturn(List.of(mockRoute));
            when(routeRepository.findByOrigin("Ate")).thenReturn(Collections.emptyList());
            when(routeRepository.findByDestination("Lima")).thenReturn(Collections.emptyList());

            List<TripOptionResponse> responses = searchRoutesUseCase.searchAvailableRoutes("Ate", "Lima");
            
            assertEquals(45, responses.get(0).totalEstimatedDuration());
            assertTrue(responses.get(0).totalEstimatedDuration() > 0);
        }
    }

}