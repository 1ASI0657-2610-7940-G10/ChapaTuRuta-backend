package com.chapaturuta.identity.application.usecase;

import com.chapaturuta.identity.application.dto.LoginRequest;
import com.chapaturuta.identity.domain.model.User;
import com.chapaturuta.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepository userRepository;

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

        String secretKey = "ChapaTuRutaSecretKeyParaFirmarLosTokensJWTDeFormaSegura2026";
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("role", user.getRole().name())
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 86400000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
}