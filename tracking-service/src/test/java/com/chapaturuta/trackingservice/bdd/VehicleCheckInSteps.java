package com.chapaturuta.trackingservice.bdd;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.application.usecase.TrackingCommandService;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    private CheckInCommand command;
    private Exception caughtException;
    private ValueOperations<String, String> valueOperationsMock;

    @Given("an active driver transmits check-in coordinates latitude {double} and longitude {double} for a route")
    public void prepareCheckInCommand(double latitude, double longitude) {
        // Pasamos null al stopId para simular un check-in intermedio (sin auto-abordaje en este test específico)
        command = new CheckInCommand(UUID.randomUUID(), UUID.randomUUID(), null, latitude, longitude, System.currentTimeMillis());

        valueOperationsMock = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperationsMock);
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

        String expectedKey = "route:" + command.routeId() + ":location";
        String expectedValue = command.latitude() + "," + command.longitude();
        verify(valueOperationsMock, times(1)).set(expectedKey, expectedValue);

        String expectedEventMessage = "Check-In registrado para ruta: " + command.routeId();
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedEventMessage
        );
    }
}