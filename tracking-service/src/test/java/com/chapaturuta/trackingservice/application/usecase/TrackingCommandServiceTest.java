package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.domain.valueobject.CoordenadasGPS;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryEntity;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingCommandServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RabbitTemplate rabbitTemplate;

    // NUEVO MOCK: Para el historial en PostgreSQL (Fase B)
    @Mock
    private TrackingHistoryRepository historyRepository;

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
                UUID.randomUUID(),
                new CoordenadasGPS(-12.0435, -76.9532),
                testTimestamp
        );
    }

    @Test
    void processCheckIn_Successful_SavesToRedisAndSendsToRabbitMQ() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

        trackingCommandService.processCheckIn(checkInCommand);

        verify(historyRepository, times(1)).save(any(TrackingHistoryEntity.class));

        String expectedKey = "route:" + checkInCommand.routeId() + ":location";
        String expectedValue = "-12.0435,-76.9532";
        verify(valueOperations, times(1)).set(expectedKey, expectedValue);

        String expectedMessage = checkInCommand.routeId() + "," + testTimestamp;
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedMessage
        );
    }

    @Test
    void processCheckIn_WithWaitingPassengers_AppliesTwoMinuteRule() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String mockPassengerKey = "route:" + checkInCommand.routeId() + ":stop:" + checkInCommand.stopId() + ":passenger:123";
        when(redisTemplate.keys(anyString())).thenReturn(Set.of(mockPassengerKey));

        trackingCommandService.processCheckIn(checkInCommand);

        verify(historyRepository, times(1)).save(any(TrackingHistoryEntity.class));

        verify(redisTemplate, times(1)).expire(mockPassengerKey, Duration.ofMinutes(2));

        String expectedMessage = checkInCommand.routeId() + "," + testTimestamp;
        verify(rabbitTemplate, times(1)).convertAndSend(
                "tracking.exchange",
                "tracking.routing.key",
                expectedMessage
        );
    }
}