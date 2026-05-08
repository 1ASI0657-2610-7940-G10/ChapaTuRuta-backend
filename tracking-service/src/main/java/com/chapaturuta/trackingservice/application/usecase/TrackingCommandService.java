package com.chapaturuta.trackingservice.application.usecase;

import com.chapaturuta.trackingservice.application.dto.CheckInCommand;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TrackingCommandService {

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public TrackingCommandService(StringRedisTemplate redisTemplate, RabbitTemplate rabbitTemplate) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void processCheckIn(CheckInCommand command) {
        // 1. Guarda en Redis (Alta velocidad)
        String key = "route:" + command.routeId() + ":location";
        String value = command.latitude() + "," + command.longitude();
        redisTemplate.opsForValue().set(key, value);

        // 2. Publica Evento Asíncrono en RabbitMQ para notificar pasajeros sin bloquear al conductor
        String eventMessage = "Check-In registrado para ruta: " + command.routeId();
        rabbitTemplate.convertAndSend("tracking.exchange", "tracking.routing.key", eventMessage);
    }
}