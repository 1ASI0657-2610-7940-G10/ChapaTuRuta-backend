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
        assertNotNull(actualResponses);
        assertFalse(actualResponses.isEmpty(), "La lista de rutas no debe estar vacía");

        // Navegamos al primer tramo (leg) del primer TripOptionResponse
        assertEquals(currentOrigin, actualResponses.get(0).legs().get(0).origin());
        assertEquals(currentDestination, actualResponses.get(0).legs().get(0).destination());

        // Validamos los totales del viaje completo
        assertNotNull(actualResponses.get(0).totalPrice());
        assertNotNull(actualResponses.get(0).totalEstimatedDuration());
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
        assertNotNull(actualResponses);
        assertTrue(actualResponses.isEmpty(), "La lista de rutas debe estar completamente vacía");
    }
}