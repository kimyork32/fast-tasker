package com.fasttasker.notification.application.dto;

import lombok.Builder;

@Builder
public record NotificationResponse(
        String id,
        String receiverTaskerId,
        String type,
        String message,
        String createdAt,
        boolean isRead,
        String status
) {}