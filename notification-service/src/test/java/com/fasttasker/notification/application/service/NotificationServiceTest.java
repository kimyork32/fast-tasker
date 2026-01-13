package com.fasttasker.notification.application.service;

import com.fasttasker.notification.application.dto.NotificationResponse;
import com.fasttasker.notification.application.mapper.NotificationMapper;
import com.fasttasker.notification.domain.INotificationRepository;
import com.fasttasker.notification.domain.Notification;
import com.fasttasker.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private INotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendNotificationSuccessfully() {
        // Given
        UUID receiverId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        NotificationType type = NotificationType.OFFER_ACCEPTED;

        // When
        notificationService.sendNotification(receiverId, offerId, type);

        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification capturedNotification = notificationCaptor.getValue();
        assertThat(capturedNotification).isNotNull();
        assertThat(capturedNotification.getReceiverTaskerId()).isEqualTo(receiverId);
        assertThat(capturedNotification.getTargetId()).isEqualTo(offerId);
        assertThat(capturedNotification.getType()).isEqualTo(type);
        // Verificamos que el mensaje se generó correctamente al construir el objeto
        assertThat(capturedNotification.getMessage()).isEqualTo("Has aceptado una oferta");
    }

    @Test
    void shouldGetAllNotificationsSuccessfully() {
        // Given
        UUID taskerId = UUID.randomUUID();
        Notification notification = new Notification(taskerId, UUID.randomUUID(), NotificationType.SYSTEM);
        NotificationResponse response = mock(NotificationResponse.class);

        when(notificationRepository.findAllByReceiverTaskerId(taskerId)).thenReturn(List.of(notification));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        // When
        List<NotificationResponse> result = notificationService.getAll(taskerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(response);
        verify(notificationRepository).findAllByReceiverTaskerId(taskerId);
        verify(notificationMapper).toNotificationResponse(notification);
    }
}