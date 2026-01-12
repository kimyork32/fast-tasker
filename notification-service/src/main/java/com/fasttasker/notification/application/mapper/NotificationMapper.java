package com.fasttasker.notification.application.mapper;

import com.fasttasker.notification.application.dto.NotificationResponse;
import com.fasttasker.notification.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    // to Response ///////////////////////////
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) return null;

        return NotificationResponse.builder()
                .id(notification.getId().toString())
                .receiverTaskerId(notification.getReceiverTaskerId().toString())
                .type(notification.getType().name())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt().toString())
                .isRead(notification.isRead())
                .status(notification.getStatus().name())
                .build();
    }
}
