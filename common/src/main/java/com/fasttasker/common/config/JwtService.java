package com.fasttasker.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(UUID accountId, UUID taskerId, boolean completedProfile) {
        return Jwts.builder()
                .subject(accountId.toString())
                .claim("taskerId", taskerId)
                .claim("profileCompleted", completedProfile)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractAccountId(String token) {
        String id = extractClaim(token, Claims::getSubject);
        return UUID.fromString(id);
    }

    public UUID extractAccountId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("cannot extract Account ID: Authentication is missing");
        }
        return (UUID) authentication.getPrincipal();
    }

    public UUID extractTaskerId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object details = authentication.getDetails();

        if (details instanceof Claims claims) {
            String taskerIdStr = claims.get("taskerId", String.class);
            return taskerIdStr != null ? UUID.fromString(taskerIdStr) : null;
        }

        log.warn("Authentication details are not of type Claims. Type: {}", details != null ? details.getClass().getName() : "null");
        return null;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}