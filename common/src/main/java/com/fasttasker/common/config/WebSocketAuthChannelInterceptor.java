package com.fasttasker.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final UserAuthenticationProvider userAuthenticationProvider;
    private final JwtService jwtService;

    public WebSocketAuthChannelInterceptor(UserAuthenticationProvider userAuthenticationProvider, JwtService jwtService) {
        this.userAuthenticationProvider = userAuthenticationProvider;
        this.jwtService = jwtService;
    }

    @Override
    @Nullable
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 1. Guard Clause: Only intercept CONNECT commands.
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        // 2. Guard Clause: Check for the Authorization header.
        List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
        if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
            log.warn("WebSocket connection attempt without Authorization header.");
            return message; // Allow connection but as an unauthenticated user.
        }

        // 3. Guard Clause: Check for the "Bearer " prefix.
        String bearerToken = authorizationHeaders.getFirst();
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.warn("WebSocket connection attempt with invalid Authorization header format.");
            return message;
        }

        // 4. Try-Catch block for robust token validation.
        try {
            String token = bearerToken.substring(7);
            Authentication auth = userAuthenticationProvider.validateToken(token);

            if (auth instanceof UsernamePasswordAuthenticationToken authToken) {
                var claims = jwtService.extractAllClaims(token);
                authToken.setDetails(claims);
            }

            accessor.setUser(auth);
            log.info("WebSocket user authenticated successfully: {}", auth.getPrincipal());
        } catch (Exception e) {
            // This will catch any errors from jwtService (e.g., expired token, malformed token).
            log.error("WebSocket authentication failed: {}", e.getMessage());
            // Optionally, you could throw an exception here to reject the connection
        }

        return message;
    }
}
