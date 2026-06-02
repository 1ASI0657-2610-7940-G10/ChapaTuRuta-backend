package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.domain.valueobject.CoordenadasGPS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingCommandServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private TrackingCommandService trackingCommandService;

    private CheckInCommand checkInCommand;
    private long testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = System.currentTimeMillis();
        checkInCommand = new CheckInCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(), // stopId simulado
                new CoordenadasGPS(-12.0435, -76.9532),
                testTimestamp
        );
    }

    @Test
    void processCheckIn_Successful_SavesToRedisAndSendsToRabbitMQ() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        trackingCommandService.processCheckIn(checkInCommand);

        String expectedKey = "route:" + checkInCommand.routeId() + ":location";
        String expectedValue = "-12.0435,-76.9532";
        verify(valueOperations, times(1)).set(expectedKey, expectedValue);

        // Se valida con el nuevo formato implementado para manejo offline
        String expectedMessage = checkInCommand.routeId() + "," + testTimestamp;
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedMessage
        );
    }
}