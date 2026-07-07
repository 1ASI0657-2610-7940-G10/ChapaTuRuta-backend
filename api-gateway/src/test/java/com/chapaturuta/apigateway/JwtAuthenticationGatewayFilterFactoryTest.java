package com.chapaturuta.apigateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationGatewayFilterFactoryTest {

    private static final String SECRET = "ChapaTuRutaSecretKeyParaFirmarLosTokensJWTDeFormaSegura2026";

    private JwtAuthenticationGatewayFilterFactory filterFactory;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filterFactory = new JwtAuthenticationGatewayFilterFactory();
        ReflectionTestUtils.setField(filterFactory, "jwtSecret", SECRET);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
    }

    @Test
    void apply_WhenPathIsPublic_DelegatesWithoutToken() {
        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/login").build()
        );

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void apply_WhenAuthorizationHeaderIsMissing_ReturnsUnauthorized() {
        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/routes").build()
        );

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void apply_WhenTokenIsInvalid_ReturnsUnauthorized() {
        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/routes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void apply_WhenTokenIsValid_DelegatesToChain() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder().subject("user-id").signWith(key).compact();
        GatewayFilter filter = filterFactory.apply(new JwtAuthenticationGatewayFilterFactory.Config());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/routes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }
}
