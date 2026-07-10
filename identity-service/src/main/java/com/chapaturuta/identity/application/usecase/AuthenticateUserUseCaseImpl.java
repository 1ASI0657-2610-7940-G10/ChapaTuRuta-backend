package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepository userRepository;

    @Value("${jwt.secret:ChapaTuRutaSecretKeyParaFirmarLosTokensJWTDeFormaSegura2026}")
    private String jwtSecret;

    public AuthenticateUserUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (!user.getPassword().equals(request.password())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("El usuario no tiene un rol asignado en la base de datos");
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 día
                .signWith(key)
                .compact();
    }
}