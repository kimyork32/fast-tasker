package com.fasttasker.notification.application.dto;

import com.fasttasker.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private UUID receiverTaskerId;
    private UUID targetId;
    private NotificationType type;
}