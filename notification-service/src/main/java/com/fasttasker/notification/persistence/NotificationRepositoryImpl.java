package com.fasttasker.notification.persistence;

import com.fasttasker.notification.domain.INotificationRepository;
import com.fasttasker.notification.domain.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {

    private final JpaNotificationRepository jpa;

    public NotificationRepositoryImpl(JpaNotificationRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification save(Notification notification) {
        return jpa.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Notification> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Notification> findByReceiverTaskerId(UUID receiverTaskerId) {
        return jpa.findByReceiverTaskerId(receiverTaskerId);
    }

    @Override
    public List<Notification> findAllByReceiverTaskerId(UUID receiverTaskerId) {
        return jpa.findAllByReceiverTaskerId(receiverTaskerId);
    }
}
