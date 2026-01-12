package com.fasttasker.notification.web.controller;

import com.fasttasker.common.config.JwtService;
import com.fasttasker.notification.application.dto.NotificationRequest;
import com.fasttasker.notification.application.dto.NotificationResponse;
import com.fasttasker.notification.application.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;

    public NotificationController(NotificationService notificationService, JwtService jwtService) {
        this.notificationService = notificationService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(request.getReceiverTaskerId(), request.getTargetId(), request.getType());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsByTasker(Authentication authentication) {
        UUID taskerId = jwtService.extractTaskerId(authentication);
        List<NotificationResponse> notifications = notificationService.getAll(taskerId);
        return ResponseEntity.ok(notifications);
    }
}