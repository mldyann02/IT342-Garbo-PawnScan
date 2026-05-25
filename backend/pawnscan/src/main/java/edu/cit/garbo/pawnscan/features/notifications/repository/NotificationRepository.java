package edu.cit.garbo.pawnscan.features.notifications.repository;

import edu.cit.garbo.pawnscan.features.notifications.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientUserIdAndReadFalse(Long recipientId);

    List<Notification> findByRecipientUserIdAndReadFalse(Long recipientId);

    void deleteByRecipientUserId(Long recipientId);
}
