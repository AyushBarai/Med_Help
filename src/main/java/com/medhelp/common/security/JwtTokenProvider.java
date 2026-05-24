package com.medhelp.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
    private final SecretKey key;
    private final long expiryHours;

    public JwtTokenProvider(
        @org.springframework.beans.factory.annotation.Value("${app.jwt.secret}") String secret,
        @org.springframework.beans.factory.annotation.Value("${app.jwt.expiry-hours}") long expiryHours){
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.expiryHours = expiryHours;
        }
    
    public String generateToken(UUID userId, UUID labId, String role){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryHours * 3600 * 1000);

        return Jwts.builder()
            .subject(userId.toString())
            .claim("labId", labId.toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    public Claims parseClaims(String token){
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token){
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT Validation Failed: {}", e.getMessage());
            return false;
        }
    }
}
