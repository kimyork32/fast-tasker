package com.fasttasker.common.config;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

/**
 * This class is responsible for validating JWT tokens and creating
 * Spring Security Authentication objects. It's used specifically
 * by the WebSocket interceptor to secure WebSocket connections.
 */
@Component
public class UserAuthenticationProvider {

    private final JwtService jwtService;

    public UserAuthenticationProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Validates the given JWT token and returns an Authentication object if valid.
     * @param token The JWT token string (without the "Bearer " prefix).
     * @return An Authentication object representing the authenticated user.
     */
    public Authentication validateToken(String token) {
        UUID accountId = jwtService.extractAccountId(token);
        Claims claims = jwtService.extractAllClaims(token);
        var auth = new UsernamePasswordAuthenticationToken(accountId, null, Collections.emptyList());
        auth.setDetails(claims);
        return auth;
    }
}