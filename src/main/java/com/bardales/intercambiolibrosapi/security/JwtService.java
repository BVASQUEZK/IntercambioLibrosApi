package com.bardales.intercambiolibrosapi.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret:${JWT_SECRET:intercambio-libros-default-secret-change-me}}") String rawSecret,
            @Value("${security.jwt.expiration-ms:86400000}") long expirationMs) {
        this.signingKey = buildSigningKey(rawSecret);
        this.expirationMs = expirationMs;
    }

    public String generateToken(int userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Integer> extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(subject));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private SecretKey buildSigningKey(String rawSecret) {
        byte[] secretBytes = rawSecret == null ? new byte[0] : rawSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            LOGGER.warn("JWT secret muy corto. Se normaliza internamente; define JWT_SECRET fuerte en entorno.");
            secretBytes = sha256(secretBytes);
        }
        if (secretBytes.length < MIN_SECRET_BYTES) {
            byte[] padded = new byte[MIN_SECRET_BYTES];
            System.arraycopy(secretBytes, 0, padded, 0, secretBytes.length);
            secretBytes = padded;
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    private byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
