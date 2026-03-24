package com.drivertracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Generate a signing key from your secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Create a JWT token for a user
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)               // who this token is for
                .claim("role", role)              // custom claim - their role
                .setIssuedAt(new Date())          // when was it created
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // when it expires
                .signWith(getSigningKey())         // sign with secret key
                .compact();                        // build the string
    }

    // Extract userId from token
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    // Extract role from token
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Check if token is still valid
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // throws if invalid or expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Parse and verify the token - throws exception if tampered or expired
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}