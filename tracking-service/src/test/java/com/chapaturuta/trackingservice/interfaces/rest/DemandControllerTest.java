package com.chapaturuta.trackingservice.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemandControllerTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final DemandController demandController = new DemandController(redisTemplate);

    @Test
    void joinQueue_SavesPassengerWithOneHourExpiration() {
        UUID routeId = UUID.randomUUID();
        UUID stopId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ResponseEntity<String> response = demandController.joinQueue(routeId, stopId, passengerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(valueOperations).set(
                "route:" + routeId + ":stop:" + stopId + ":passenger:" + passengerId,
                "waiting",
                Duration.ofHours(1)
        );
    }

    @Test
    void getRouteDemand_GroupsPassengersByStop() {
        UUID routeId = UUID.randomUUID();
        UUID stopOne = UUID.randomUUID();
        UUID stopTwo = UUID.randomUUID();
        when(redisTemplate.keys("route:" + routeId + ":stop:*:passenger:*")).thenReturn(Set.of(
                "route:" + routeId + ":stop:" + stopOne + ":passenger:" + UUID.randomUUID(),
                "route:" + routeId + ":stop:" + stopOne + ":passenger:" + UUID.randomUUID(),
                "route:" + routeId + ":stop:" + stopTwo + ":passenger:" + UUID.randomUUID()
        ));

        ResponseEntity<Map<String, Integer>> response = demandController.getRouteDemand(routeId);

        assertEquals(2, response.getBody().get(stopOne.toString()));
        assertEquals(1, response.getBody().get(stopTwo.toString()));
    }

    @Test
    void registerTransfer_SavesPassengerAsWaitingTransfer() {
        UUID nextRouteId = UUID.randomUUID();
        UUID nextStopId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        ResponseEntity<String> response = demandController.registerTransfer(nextRouteId, nextStopId, passengerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(valueOperations).set(
                "route:" + nextRouteId + ":stop:" + nextStopId + ":passenger:" + passengerId,
                "waiting_transfer",
                Duration.ofHours(1)
        );
    }

    @Test
    void leaveQueue_WhenPassengerExists_ReturnsOk() {
        UUID routeId = UUID.randomUUID();
        UUID stopId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();
        when(redisTemplate.delete("route:" + routeId + ":stop:" + stopId + ":passenger:" + passengerId))
                .thenReturn(true);

        ResponseEntity<String> response = demandController.leaveQueue(routeId, stopId, passengerId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void leaveQueue_WhenPassengerDoesNotExist_Returns404() {
        UUID routeId = UUID.randomUUID();
        UUID stopId = UUID.randomUUID();
        UUID passengerId = UUID.randomUUID();
        when(redisTemplate.delete("route:" + routeId + ":stop:" + stopId + ":passenger:" + passengerId))
                .thenReturn(false);

        ResponseEntity<String> response = demandController.leaveQueue(routeId, stopId, passengerId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
