package com.fasttasker.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private UUID receiverId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        receiverId = UUID.randomUUID();
        targetId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        void shouldCreateNotificationSuccessfully() {
            NotificationType type = NotificationType.SYSTEM;
            Notification notification = new Notification(receiverId, targetId, type);

            assertThat(notification).isNotNull();
            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getReceiverTaskerId()).isEqualTo(receiverId);
            assertThat(notification.getTargetId()).isEqualTo(targetId);
            assertThat(notification.getType()).isEqualTo(type);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getCreatedAt()).isNotNull();
            assertThat(notification.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(notification.getMessage()).isEqualTo("Notificación del sistema");
        }

        @Test
        void shouldCreateNotificationUsingBuilder() {
            NotificationType type = NotificationType.OFFER_ACCEPTED;
            Notification notification = Notification.builder()
                    .receiverTaskerId(receiverId)
                    .targetId(targetId)
                    .type(type)
                    .build();

            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getType()).isEqualTo(type);
            assertThat(notification.getMessage()).isEqualTo("Has aceptado una oferta");
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        }
    }

    @Nested
    class BusinessLogicTests {

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        void shouldGenerateCorrectMessageForEveryType(NotificationType type) {
            Notification notification = new Notification(receiverId, targetId, type);

            String expectedMessage = switch (type) {
                case QUESTION -> "Tienes una nueva pregunta";
                case OFFER_ACCEPTED -> "Has aceptado una oferta";
                case SYSTEM -> "Notificación del sistema";
                case TASK_COMPLETED -> "Has completado una tarea :)";
                case NEW_MESSAGE -> "Tienes un nuevo mensaje";
            };

            assertThat(notification.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        void shouldUpdateMessageContentWhenTypeChanges() {
            // Given
            Notification notification = new Notification(receiverId, targetId, NotificationType.SYSTEM);
            assertThat(notification.getMessage()).isEqualTo("Notificación del sistema");

            // When
            notification.setType(NotificationType.TASK_COMPLETED);
            notification.createMessageContent();

            // Then
            assertThat(notification.getMessage()).isEqualTo("Has completado una tarea :)");
        }
    }
}