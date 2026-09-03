package com.ikhsan.securepaywallet.auth.security;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration)

    {
        this.secretKey = Keys.hmacShaKeyFor(
                io.jsonwebtoken.io.Decoders.BASE64.decode(secret));

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }

    public String generateAccessToken(UUID userId, String role, UUID sessionId) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("sessionId", sessionId.toString())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userId, UUID sessionId) {

        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("sessionId", sessionId.toString())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractTokenType(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    public String extractSessionId(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("sessionId", String.class);
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return "access".equals(
                extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(
                extractTokenType(token));
    }
}
