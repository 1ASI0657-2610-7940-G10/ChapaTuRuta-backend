package com.chapaturuta.routing.bdd;

import com.chapaturuta.routing.application.dto.TripOptionResponse;
import com.chapaturuta.routing.application.usecase.SearchRoutesUseCase;
import com.chapaturuta.routing.domain.model.Route;
import com.chapaturuta.routing.domain.repository.RouteRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@CucumberContextConfiguration
@SpringBootTest
public class SearchRoutesSteps {

    @Autowired
    private SearchRoutesUseCase searchRoutesUseCase;

    @MockitoBean
    private RouteRepository routeRepository;

    // Cambiado de RouteResponse a TripOptionResponse
    private List<TripOptionResponse> actualResponses;
    private String currentOrigin;
    private String currentDestination;

    @Given("the system has available routes registered from {string} to {string}")
    public void setupAvailableRoutes(String origin, String destination) {
        Route sampleRoute = Route.builder()
                .id(UUID.randomUUID())
                .originDistrict(origin)
                .destinationDistrict(destination)
                .price(4.00)
                .durationMin(35)
                .build();

        // 1. Simulamos que sí encuentra la ruta directa
        when(routeRepository.findRoutes(origin, destination)).thenReturn(List.of(sampleRoute));

        // 2. Simulamos listas vacías para el algoritmo de transbordos para no alterar este test
        when(routeRepository.findByOrigin(origin)).thenReturn(Collections.emptyList());
        when(routeRepository.findByDestination(destination)).thenReturn(Collections.emptyList());
    }

    @When("a user searches for available routes from {string} to {string}")
    public void executeSearch(String origin, String destination) {
        this.currentOrigin = origin;
        this.currentDestination = destination;
        this.actualResponses = searchRoutesUseCase.searchAvailableRoutes(origin, destination);
    }

    @Then("the system returns a list of routes containing pricing and duration details")
    public void verifyResultsNotEmpty() {
        assertNotNull(actualResponses, "La lista de rutas no debe ser nula");
        assertFalse(actualResponses.isEmpty(), "La lista de rutas no debe estar vacía");

        // Navegamos al primer tramo (leg) del primer TripOptionResponse
        assertEquals(currentOrigin, actualResponses.get(0).legs().get(0).origin());
        assertEquals(currentDestination, actualResponses.get(0).legs().get(0).destination());

        // Validamos los totales del viaje completo
        assertNotNull(actualResponses.get(0).totalPrice(), "El precio total no debe ser nulo");
        assertNotNull(actualResponses.get(0).totalEstimatedDuration(), "La duración estimada no debe ser nula");
        assertTrue(actualResponses.get(0).totalPrice() > 0, "El precio debe ser mayor a 0");
        assertTrue(actualResponses.get(0).totalEstimatedDuration() > 0, "La duración debe ser mayor a 0");
    }

    @Given("there are no routes registered from {string} to {string}")
    public void setupNoCoverage(String origin, String destination) {
        // Simulamos que no hay ni directas ni opciones para transbordo
        when(routeRepository.findRoutes(origin, destination)).thenReturn(Collections.emptyList());
        when(routeRepository.findByOrigin(origin)).thenReturn(Collections.emptyList());
        when(routeRepository.findByDestination(destination)).thenReturn(Collections.emptyList());
    }

    @Then("the system returns an empty list of routes")
    public void verifyResultsEmpty() {
        assertNotNull(actualResponses, "La lista de rutas no debe ser nula");
        assertTrue(actualResponses.isEmpty(), "La lista de rutas debe estar completamente vacía");
    }

    @Given("the system has a transfer route from {string} via {string} to {string}")
    public void setupTransferRoute(String origin, String via, String destination) {
        Route leg1 = Route.builder()
                .id(UUID.randomUUID())
                .originDistrict(origin)
                .destinationDistrict(via)
                .price(3.00)
                .durationMin(30)
                .build();

        Route leg2 = Route.builder()
                .id(UUID.randomUUID())
                .originDistrict(via)
                .destinationDistrict(destination)
                .price(2.50)
                .durationMin(20)
                .build();

        when(routeRepository.findRoutes(origin, destination)).thenReturn(Collections.emptyList());
        when(routeRepository.findByOrigin(origin)).thenReturn(List.of(leg1));
        when(routeRepository.findByDestination(destination)).thenReturn(List.of(leg2));
    }

    @Then("the system returns routes with multiple legs")
    public void verifyTransferRoutes() {
        assertNotNull(actualResponses);
        assertFalse(actualResponses.isEmpty(), "Debe haber al menos una opción de ruta");

        // Verificar que existe al menos una ruta con múltiples tramos
        boolean hasTransferRoute = actualResponses.stream()
                .anyMatch(tripOption -> tripOption.legs().size() == 2);

        assertTrue(hasTransferRoute, "Debe haber al menos una ruta con transbordo (2 tramos)");

        // Validar que el precio total incluye ambos tramos
        TripOptionResponse transferRoute = actualResponses.stream()
                .filter(tripOption -> tripOption.legs().size() == 2)
                .findFirst()
                .orElse(null);

        assertNotNull(transferRoute);
        Double expectedPrice = transferRoute.legs().get(0).price() + transferRoute.legs().get(1).price();
        assertEquals(expectedPrice, transferRoute.totalPrice());

        // Validar que la duración incluye espera de transbordo (5 minutos)
        Integer expectedDuration = transferRoute.legs().get(0).estimatedDuration() +
                transferRoute.legs().get(1).estimatedDuration() + 5;
        assertEquals(expectedDuration, transferRoute.totalEstimatedDuration());
    }

    @Given("the system has {int} available routes from {string} to {string}")
    public void setupMultipleRoutes(int count, String origin, String destination) {
        List<Route> routes = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            routes.add(Route.builder()
                    .id(UUID.randomUUID())
                    .originDistrict(origin)
                    .destinationDistrict(destination)
                    .price(3.00 + i * 0.5)
                    .durationMin(30 + i * 5)
                    .build());
        }

        when(routeRepository.findRoutes(origin, destination)).thenReturn(routes);
        when(routeRepository.findByOrigin(origin)).thenReturn(Collections.emptyList());
        when(routeRepository.findByDestination(destination)).thenReturn(Collections.emptyList());
    }

    @Then("the system returns {int} available routes")
    public void verifyMultipleRoutes(int expectedCount) {
        assertNotNull(actualResponses);
        assertEquals(expectedCount, actualResponses.size(), 
                "Se esperaban " + expectedCount + " rutas pero se obtuvieron " + actualResponses.size());
    }

    @Then("each route option has a valid price and duration")
    public void verifyEachRouteHasValidData() {
        assertNotNull(actualResponses);
        for (TripOptionResponse route : actualResponses) {
            assertNotNull(route.totalPrice(), "El precio no debe ser nulo");
            assertNotNull(route.totalEstimatedDuration(), "La duración no debe ser nula");
            assertTrue(route.totalPrice() > 0, "El precio debe ser mayor a 0");
            assertTrue(route.totalEstimatedDuration() > 0, "La duración debe ser mayor a 0");
            assertFalse(route.legs().isEmpty(), "Cada ruta debe tener al menos un tramo");
        }
    }
}