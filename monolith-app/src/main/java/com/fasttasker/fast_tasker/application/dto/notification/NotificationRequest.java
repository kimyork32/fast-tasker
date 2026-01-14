package com.fasttasker.fast_tasker.application.dto.notification;

import com.fasttasker.fast_tasker.domain.notification.NotificationType;
import java.util.UUID;

public record NotificationRequest(
    UUID receiverTaskerId,
    UUID targetId,
    NotificationType type
) {}