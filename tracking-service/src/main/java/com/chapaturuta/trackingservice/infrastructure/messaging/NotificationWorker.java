package com.chapaturuta.trackingservice.infrastructure.messaging;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationWorker {

    // Este método está "escuchando" constantemente a RabbitMQ en segundo plano
    @RabbitListener(queues = "tracking.queue")
    public void processCheckInEvent(String eventMessage) {
        System.out.println("Worker recibió el evento: " + eventMessage);

        try {

            String deviceToken = "token_del_celular_del_pasajero";

            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle("¡Tu colectivo está cerca!")
                            .setBody("El vehículo de la ruta acaba de hacer check-in en el paradero anterior.")
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