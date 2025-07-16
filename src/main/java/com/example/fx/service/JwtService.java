package com.example.fx.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        logger.info("JWT secret key initialized");
    }

    public String generateToken(String username) {
        logger.info("Generating token for user: {}", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        logger.debug("Token generated: {}", token);
        return token;
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            logger.info("Username extracted from token: {}", username);
            return username;
        } catch (JwtException e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            boolean valid = extractUsername(token).equals(username) && !isTokenExpired(token);
            logger.info("Token valid for {}: {}", username, valid);
            return valid;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        boolean expired = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
        if (expired) {
            logger.info("Token has expired");
        }
        return expired;
    }

    public String validateToken(String token) {
        try {
            String username = extractUsername(token);
            logger.info("Token validated for user: {}", username);
            return username;
        } catch (Exception e) {
            logger.warn("Invalid token: {}", e.getMessage());
            return null;
        }
    }
}
