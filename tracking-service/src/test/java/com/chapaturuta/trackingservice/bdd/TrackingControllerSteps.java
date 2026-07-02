package com.chapaturuta.trackingservice.bdd;

import com.chapaturuta.trackingservice.application.dto.CheckInRequest;
import com.chapaturuta.trackingservice.application.dto.EtaQueryResponse;
import com.chapaturuta.trackingservice.application.usecase.TrackingCommandService;
import com.chapaturuta.trackingservice.application.usecase.TrackingQueryService;
import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.interfaces.rest.TrackingController;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CucumberContextConfiguration
@WebMvcTest(TrackingController.class)
public class TrackingControllerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrackingCommandService trackingCommandService;

    @MockitoBean
    private TrackingQueryService trackingQueryService;

    private CheckInRequest checkInRequest;
    private UUID etaRouteId;
    private double passengerLat;
    private double passengerLng;
    private ResultActions lastResult;

    @Given("a valid check-in request for the tracking controller")
    public void givenValidCheckInRequest() {
        checkInRequest = new CheckInRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                -12.0435,
                -76.9532,
                System.currentTimeMillis()
        );

        doNothing().when(trackingCommandService).processCheckIn(any(CheckInCommand.class));
    }

    @Given("an invalid check-in request with latitude {double} and longitude {double}")
    public void givenInvalidCheckInRequest(double latitude, double longitude) {
        checkInRequest = new CheckInRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                latitude,
                longitude,
                System.currentTimeMillis()
        );
    }

    @When("the client sends the check-in request")
    public void whenClientSendsCheckInRequest() throws Exception {
        lastResult = mockMvc.perform(post("/api/v1/tracking/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkInRequest)));
    }

    @Then("the response status should be {int}")
    public void thenResponseStatusShouldBe(int expectedStatus) throws Exception {
        lastResult.andExpect(status().is(expectedStatus));
    }

    @Then("the response body should confirm asynchronous processing")
    public void thenResponseBodyShouldConfirmAsyncProcessing() throws Exception {
        lastResult.andExpect(content().string("Check-in procesado asíncronamente"));
        verify(trackingCommandService).processCheckIn(any(CheckInCommand.class));
    }

    @Then("the response body should describe the validation error")
    public void thenResponseBodyShouldDescribeValidationError() throws Exception {
        lastResult.andExpect(content().string(Matchers.containsString("Error de validación")));
    }

    @Given("the ETA service returns a result for route {string}")
    public void givenEtaServiceReturnsAResultForRoute(String routeId) {
        etaRouteId = UUID.fromString(routeId);
        passengerLat = -12.0435;
        passengerLng = -76.9532;

        when(trackingQueryService.getRouteEta(eq(etaRouteId), eq(passengerLat), eq(passengerLng)))
                .thenReturn(new EtaQueryResponse(etaRouteId, -12.045, -76.954, "15 min"));
    }

    @Given("the ETA service cannot resolve route {string}")
    public void givenEtaServiceCannotResolveRoute(String routeId) {
        etaRouteId = UUID.fromString(routeId);
        passengerLat = -12.0435;
        passengerLng = -76.9532;

        when(trackingQueryService.getRouteEta(eq(etaRouteId), eq(passengerLat), eq(passengerLng)))
                .thenThrow(new IllegalArgumentException("No hay vehículos de esta ruta emitiendo señal actualmente."));
    }

    @When("the client requests the ETA for route {string} and passenger coordinates {double} and {double}")
    public void whenClientRequestsEta(String routeId, double latitude, double longitude) throws Exception {
        lastResult = mockMvc.perform(get("/api/v1/tracking/eta/{routeId}", routeId)
                .param("pasajeroLat", String.valueOf(latitude))
                .param("pasajeroLng", String.valueOf(longitude)));
    }

    @Then("the ETA payload should contain the route identifier and current vehicle coordinates")
    public void thenEtaPayloadShouldContainTheRouteIdentifierAndCurrentVehicleCoordinates() throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value(etaRouteId.toString()))
                .andExpect(jsonPath("$.currentLatitude").value(-12.045))
                .andExpect(jsonPath("$.currentLongitude").value(-76.954))
                .andExpect(jsonPath("$.estimatedTime").value("15 min"));

        verify(trackingQueryService).getRouteEta(eq(etaRouteId), eq(passengerLat), eq(passengerLng));
    }
}

