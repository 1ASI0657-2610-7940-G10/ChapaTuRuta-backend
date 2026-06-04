package com.chapaturuta.trackingservice.bdd;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.application.usecase.TrackingCommandService;
import com.chapaturuta.trackingservice.domain.valueobject.CoordenadasGPS;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryEntity;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@CucumberContextConfiguration
@SpringBootTest
public class VehicleCheckInSteps {

    @Autowired
    private TrackingCommandService trackingCommandService;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private TrackingHistoryRepository trackingHistoryRepository;

    private CheckInCommand command;
    private Exception caughtException;
    private ValueOperations<String, String> valueOperationsMock;
    private long testTimestamp;
    private double latitude;
    private double longitude;
    private String waitingPassengerKey;

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> createValueOperationsMock() {
        return mock(ValueOperations.class);
    }

    @Given("an active driver transmits check-in coordinates latitude {double} and longitude {double} for a route")
    public void prepareCheckInCommand(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        testTimestamp = System.currentTimeMillis();

        command = new CheckInCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                new CoordenadasGPS(latitude, longitude),
                testTimestamp
        );

        valueOperationsMock = createValueOperationsMock();
        when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
        waitingPassengerKey = null;
        caughtException = null;
    }

    @Given("the route has waiting passengers at stop {string}")
    public void prepareWaitingPassengersAtStop(String stopId) {
        UUID stopUuid = UUID.fromString(stopId);

        command = new CheckInCommand(
                command.driverId(),
                command.routeId(),
                stopUuid,
                new CoordenadasGPS(latitude, longitude),
                testTimestamp
        );

        waitingPassengerKey = "route:" + command.routeId() + ":stop:" + command.stopId() + ":passenger:123";
        when(redisTemplate.keys("route:" + command.routeId() + ":stop:" + command.stopId() + ":passenger:*"))
                .thenReturn(Set.of(waitingPassengerKey));
    }

    @When("the check-in command is processed by the command service")
    public void executeProcessCheckIn() {
        try {
            trackingCommandService.processCheckIn(command);
        } catch (Exception e) {
            this.caughtException = e;
        }
    }

    @Then("the system updates the vehicle location in cache and emits an async notification event")
    public void verifyCheckInBehavior() {
        assertNull(caughtException, "No debió lanzarse ninguna excepción durante el procesamiento");

        verify(trackingHistoryRepository, times(1)).save(any(TrackingHistoryEntity.class));

        String expectedKey = "route:" + command.routeId() + ":location";
        String expectedValue = command.coordenadas().latitude() + "," + command.coordenadas().longitude();
        verify(valueOperationsMock, times(1)).set(expectedKey, expectedValue);

        String expectedEventMessage = command.routeId() + "," + testTimestamp;
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedEventMessage
        );
    }

    @Then("the waiting passengers at the stop receive a two-minute extension")
    public void verifyWaitingPassengersExtension() {
        assertNotNull(command.stopId(), "El escenario debe incluir un stopId para extender a pasajeros en espera");
        assertNotNull(waitingPassengerKey, "Debe existir una clave de pasajero simulada para esta rama");

        String expectedPattern = "route:" + command.routeId() + ":stop:" + command.stopId() + ":passenger:*";
        verify(redisTemplate, times(1)).keys(expectedPattern);
        verify(redisTemplate, times(1)).expire(waitingPassengerKey, Duration.ofMinutes(2));
    }
}