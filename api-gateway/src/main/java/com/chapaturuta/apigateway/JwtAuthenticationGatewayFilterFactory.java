package com.chapaturuta.apigateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    // Lee el mismo secreto exacto que usa el identity-service
    @Value("${jwt.secret:ChapaTuRutaSecretKeyParaFirmarLosTokensJWTDeFormaSegura2026}")
    private String jwtSecret;

    public JwtAuthenticationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // 1. Rutas públicas (Login y Registro pasan directo)
            if (path.contains("/auth/register") || path.contains("/auth/login") || path.contains("/api-docs") || path.contains("/swagger-ui")) {
                return chain.filter(exchange);
            }

            // 2. Extraer el Header de Autorización
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.replace("Bearer ", "");

            try {
                // 3. Generar la llave
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                // 4. Validación estricta con JJWT 0.12.6 (Firma y Fecha de Expiración)
                Claims claims = Jwts.parser()
                        .verifyWith(key) // Este método es exclusivo de las versiones nuevas
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

            } catch (Exception e) {
                // Si el token es falso o expiró
                System.err.println("Error de validación JWT: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // 5. El token es válido, continúa hacia el microservicio
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}