package com.chapaturuta.routing.bdd;

import com.chapaturuta.routing.application.dto.RouteResponse;
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

    private List<RouteResponse> actualResponses;
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

        when(routeRepository.findRoutes(origin, destination)).thenReturn(List.of(sampleRoute));
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
        assertEquals(currentOrigin, actualResponses.get(0).origin());
        assertEquals(currentDestination, actualResponses.get(0).destination());
        assertNotNull(actualResponses.get(0).price());
        assertNotNull(actualResponses.get(0).estimatedDuration());
    }

    @Given("there are no routes registered from {string} to {string}")
    public void setupNoCoverage(String origin, String destination) {
        when(routeRepository.findRoutes(origin, destination)).thenReturn(Collections.emptyList());
    }

    @Then("the system returns an empty list of routes")
    public void verifyResultsEmpty() {
        assertNotNull(actualResponses);
        assertTrue(actualResponses.isEmpty(), "La lista de rutas debe estar completamente vacía");
    }
}