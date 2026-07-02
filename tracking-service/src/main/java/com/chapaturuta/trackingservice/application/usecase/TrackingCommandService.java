package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryEntity;
import com.chapaturuta.trackingservice.infrastructure.persistence.TrackingHistoryRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Service
public class TrackingCommandService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final TrackingHistoryRepository historyRepository;

    public TrackingCommandService(StringRedisTemplate redisTemplate,
                                  RabbitTemplate rabbitTemplate,
                                  TrackingHistoryRepository historyRepository) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.historyRepository = historyRepository;
    }

    public void processCheckIn(CheckInCommand command) {
        long eventTimestamp = command.timestamp() != null ? command.timestamp() : System.currentTimeMillis();

        TrackingHistoryEntity historyRecord = TrackingHistoryEntity.builder()
                .driverId(command.driverId())
                .routeId(command.routeId())
                .latitude(command.coordenadas().latitude())
                .longitude(command.coordenadas().longitude())
                .recordedAt(Instant.ofEpochMilli(eventTimestamp))
                .build();
        historyRepository.save(historyRecord);

        String key = "route:" + command.routeId() + ":location";
        String value = command.coordenadas().latitude() + "," + command.coordenadas().longitude();
        redisTemplate.opsForValue().set(key, value);

        if (command.stopId() != null) {
            String passengerPattern = "route:" + command.routeId() + ":stop:" + command.stopId() + ":passenger:*";
            Set<String> waitingPassengers = redisTemplate.keys(passengerPattern);

            if (waitingPassengers != null && !waitingPassengers.isEmpty()) {
                for (String passengerKey : waitingPassengers) {
                    redisTemplate.expire(passengerKey, Duration.ofMinutes(2));
                }
            }
        }

        String eventMessage = command.routeId() + "," + eventTimestamp;
        rabbitTemplate.convertAndSend("tracking.exchange", "tracking.routing.key", eventMessage);
    }
}