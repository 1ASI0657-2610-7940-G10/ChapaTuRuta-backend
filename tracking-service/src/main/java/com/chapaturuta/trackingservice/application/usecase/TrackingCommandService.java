package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class TrackingCommandService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public TrackingCommandService(StringRedisTemplate redisTemplate, RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void processCheckIn(CheckInCommand command) {
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
                System.out.println("Auto-abordaje: Se actualizó TTL a 2 min para " + waitingPassengers.size() + " pasajeros del paradero " + command.stopId());
            }
        }

        long eventTimestamp = command.timestamp() != null ? command.timestamp() : System.currentTimeMillis();
        String eventMessage = command.routeId() + "," + eventTimestamp;
        rabbitTemplate.convertAndSend("tracking.exchange", "tracking.routing.key", eventMessage);
    }
}