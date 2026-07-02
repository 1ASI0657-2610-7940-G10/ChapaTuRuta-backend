package com.chapaturuta.routing.infrastructure.persistence;

import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.infrastructure.SpringDataRouteRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RouteRepositoryAdapter - Tests")
class RouteRepositoryAdapterTest {

    @Mock
    private SpringDataRouteRepository springDataRepository;

    @InjectMocks
    private RouteRepositoryAdapter routeRepositoryAdapter;

    private RouteEntity mockEntity;
    private Route mockRoute;
    private UUID routeId;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        mockEntity = RouteEntity.builder()
                .id(routeId)
                .originDistrict("Ate")
                .destinationDistrict("Lima")
                .price(3.50)
                .durationMin(45)
                .build();

        mockRoute = Route.builder()
                .id(routeId)
                .originDistrict("Ate")
                .destinationDistrict("Lima")
                .price(3.50)
                .durationMin(45)
                .build();
    }

    @Nested
    @DisplayName("Buscar rutas directas")
    class FindRoutesTests {

        @Test
        @DisplayName("Debe encontrar rutas directas por origen y destino (case insensitive)")
        void findRoutes_ReturnsDirectRoutes() {
            when(springDataRepository.findByOriginDistrictIgnoreCaseAndDestinationDistrictIgnoreCase("Ate", "Lima"))
                    .thenReturn(List.of(mockEntity));

            List<Route> routes = routeRepositoryAdapter.findRoutes("Ate", "Lima");

            assertNotNull(routes);
            assertEquals(1, routes.size());
            assertEquals("Ate", routes.get(0).getOriginDistrict());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay rutas")
        void findRoutes_NoResults_ReturnsEmptyList() {
            when(springDataRepository.findByOriginDistrictIgnoreCaseAndDestinationDistrictIgnoreCase("Surco", "Callao"))
                    .thenReturn(Collections.emptyList());

            List<Route> routes = routeRepositoryAdapter.findRoutes("Surco", "Callao");

            assertTrue(routes.isEmpty());
        }
    }

    @Nested
    @DisplayName("Buscar por origen")
    class FindByOriginTests {

        @Test
        @DisplayName("Debe encontrar rutas por origen (case insensitive)")
        void findByOrigin_ReturnsRoutes() {
            when(springDataRepository.findByOriginDistrictIgnoreCase("Ate"))
                    .thenReturn(List.of(mockEntity));

            List<Route> routes = routeRepositoryAdapter.findByOrigin("Ate");

            assertNotNull(routes);
            assertEquals(1, routes.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay rutas con ese origen")
        void findByOrigin_NoResults_ReturnsEmptyList() {
            when(springDataRepository.findByOriginDistrictIgnoreCase("NoExiste"))
                    .thenReturn(Collections.emptyList());

            List<Route> routes = routeRepositoryAdapter.findByOrigin("NoExiste");

            assertTrue(routes.isEmpty());
        }
    }

    @Nested
    @DisplayName("Buscar por destino")
    class FindByDestinationTests {

        @Test
        @DisplayName("Debe encontrar rutas por destino (case insensitive)")
        void findByDestination_ReturnsRoutes() {
            when(springDataRepository.findByDestinationDistrictIgnoreCase("Lima"))
                    .thenReturn(List.of(mockEntity));

            List<Route> routes = routeRepositoryAdapter.findByDestination("Lima");

            assertNotNull(routes);
            assertEquals(1, routes.size());
        }
    }

    @Nested
    @DisplayName("Guardar rutas")
    class SaveRouteTests {

        @Test
        @DisplayName("Debe guardar una ruta correctamente")
        void save_SavesRoute() {
            when(springDataRepository.save(any(RouteEntity.class)))
                    .thenReturn(mockEntity);

            Route savedRoute = routeRepositoryAdapter.save(mockRoute);

            assertNotNull(savedRoute);
            assertEquals(routeId, savedRoute.getId());
            verify(springDataRepository).save(any(RouteEntity.class));
        }
    }

    @Nested
    @DisplayName("Buscar por ID")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar una ruta por ID")
        void findById_ReturnsRoute() {
            when(springDataRepository.findById(routeId))
                    .thenReturn(Optional.of(mockEntity));

            Optional<Route> route = routeRepositoryAdapter.findById(routeId);

            assertTrue(route.isPresent());
            assertEquals(routeId, route.get().getId());
        }

        @Test
        @DisplayName("Debe retornar Optional vacío cuando no existe")
        void findById_NotFound_ReturnsEmpty() {
            when(springDataRepository.findById(routeId))
                    .thenReturn(Optional.empty());

            Optional<Route> route = routeRepositoryAdapter.findById(routeId);

            assertFalse(route.isPresent());
        }
    }

    @Nested
    @DisplayName("Obtener todas las rutas")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar todas las rutas")
        void findAll_ReturnsAllRoutes() {
            when(springDataRepository.findAll()).thenReturn(List.of(mockEntity));

            List<Route> routes = routeRepositoryAdapter.findAll();

            assertEquals(1, routes.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay rutas")
        void findAll_NoRoutes_ReturnsEmptyList() {
            when(springDataRepository.findAll()).thenReturn(Collections.emptyList());

            List<Route> routes = routeRepositoryAdapter.findAll();

            assertTrue(routes.isEmpty());
        }
    }

    @Nested
    @DisplayName("Eliminar rutas")
    class DeleteTests {

        @Test
        @DisplayName("Debe eliminar una ruta por ID")
        void deleteById_DeletesRoute() {
            routeRepositoryAdapter.deleteById(routeId);

            verify(springDataRepository).deleteById(routeId);
        }
    }
}

