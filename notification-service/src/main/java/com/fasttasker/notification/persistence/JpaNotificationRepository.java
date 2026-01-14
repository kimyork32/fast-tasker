package com.fasttasker.notification.persistence;

import com.fasttasker.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 
 */
@Repository
public interface JpaNotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByReceiverTaskerId(UUID receiverTaskerId);

    List<Notification> findAllByReceiverTaskerId(UUID receiverTaskerId);
    // NOTE: Spring implemented the other methods (query methods conversions)

}