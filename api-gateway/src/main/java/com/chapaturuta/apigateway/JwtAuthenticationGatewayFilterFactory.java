package com.chapaturuta.apigateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    public JwtAuthenticationGatewayFilterFactory() { super(Config.class); }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Rutas públicas que no piden token
            if (path.contains("/auth/register") || path.contains("/auth/login") || path.contains("/api-docs")) {
                return chain.filter(exchange);
            }

            // Forma compatible y segura de obtener el Header en Spring Boot 4.x
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Validar si el header existe y si tiene el formato correcto
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extraer el token
            String token = authHeader.replace("Bearer ", "");

            // Validar token vacío
            if(token.isEmpty()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // (Aquí iría la validación de la firma del JWT)

            return chain.filter(exchange);
        };
    }

    public static class Config {}
}