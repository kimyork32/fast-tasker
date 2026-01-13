package com.fasttasker.notification.application.service;

import com.fasttasker.notification.application.dto.NotificationRequest;
import com.fasttasker.notification.application.dto.NotificationResponse;
import com.fasttasker.notification.application.mapper.NotificationMapper;
import com.fasttasker.notification.config.NotificationRabbitMQConfig;
import com.fasttasker.notification.domain.INotificationRepository;
import com.fasttasker.notification.domain.Notification;
import com.fasttasker.notification.domain.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final INotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(SimpMessagingTemplate messagingTemplate, INotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @RabbitListener(queues = NotificationRabbitMQConfig.QUEUE_NAME)
    public void handleNotification(NotificationRequest request) {
        sendNotification(request.getReceiverTaskerId(), request.getTargetId(), request.getType());
    }

    /**
     * @param receiverTaskerId tasker that receive notification
     * @param type type of the notification
     */
    public void sendNotification(UUID receiverTaskerId, UUID offerId, NotificationType type) {

        log.info("called");
        var notification = Notification.builder()
                .receiverTaskerId(receiverTaskerId)
                .targetId(offerId)
                .type(type)
                .build();

        // save notification
        Notification savedNotification = notificationRepository.save(notification);

        log.info("convertAndSend (Topic): {}", receiverTaskerId.toString());
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + receiverTaskerId,
                notificationMapper.toNotificationResponse(savedNotification)
        );
    }

    /**
     * @param taskerId
     */
    public void getUnread(UUID taskerId) {
        // TODO implement here
    }

    /**
     * @param notificationId
     */
    public void markAsRead(UUID notificationId) {
        // TODO implement here
    }

    public List<NotificationResponse> getAll(UUID taskerId) {
        List<Notification> notifications = notificationRepository.findAllByReceiverTaskerId(taskerId);

        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

}