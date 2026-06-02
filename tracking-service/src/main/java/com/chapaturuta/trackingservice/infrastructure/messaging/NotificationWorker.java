package com.chapaturuta.trackingservice.infrastructure.messaging;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationWorker {

    private static final long MAX_DELAY_MS = 60000;

    @RabbitListener(queues = "tracking.queue")
    public void processCheckInEvent(String eventMessage) {
        System.out.println("Worker recibió el evento: " + eventMessage);

        try {
            String[] parts = eventMessage.split(",");
            if (parts.length < 2) return;

            String routeId = parts[0];
            long eventTimestamp = Long.parseLong(parts[1]);
            long currentTimestamp = System.currentTimeMillis();

            if (currentTimestamp - eventTimestamp > MAX_DELAY_MS) {
                System.out.println("Evento descartado para notificación Push por antigüedad. Ruta: " + routeId);
                return;
            }

            String deviceToken = "token_del_celular_del_pasajero";

            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle("¡Tu colectivo está cerca!")
                            .setBody("El vehículo de tu ruta acaba de hacer check-in cerca de ti.")
                            .build())
                    .setToken(deviceToken)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notificación Push enviada con éxito: " + response);

        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }
}