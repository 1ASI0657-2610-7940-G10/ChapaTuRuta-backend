package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
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

    @BeforeEach
    void setUp() {
        checkInCommand = new CheckInCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                -12.0435,
                -76.9532
        );
    }

    @Test
    void processCheckIn_Successful_SavesToRedisAndSendsToRabbitMQ() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        trackingCommandService.processCheckIn(checkInCommand);

        String expectedKey = "route:" + checkInCommand.routeId() + ":location";
        String expectedValue = "-12.0435,-76.9532";
        verify(valueOperations, times(1)).set(expectedKey, expectedValue);

        String expectedMessage = "Check-In registrado para ruta: " + checkInCommand.routeId();
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedMessage
        );
    }
}