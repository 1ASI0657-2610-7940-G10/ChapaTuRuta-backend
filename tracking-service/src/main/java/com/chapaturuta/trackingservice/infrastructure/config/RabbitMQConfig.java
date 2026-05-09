package com.chapaturuta.trackingservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Lee los valores que pusiste en tu application.yml
    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String queueName;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    // 1. Crea la Cola si no existe
    @Bean
    public Queue queue() {
        return new Queue(queueName, true); // true = durable (sobrevive a reinicios de Docker)
    }

    // 2. Crea el Exchange (El enrutador de mensajes)
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    // 3. Une la Cola con el Exchange usando el Routing Key
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}