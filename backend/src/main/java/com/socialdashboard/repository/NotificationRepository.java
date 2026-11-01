package com.socialdashboard.repository;

import com.socialdashboard.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    void deleteByIdAndRecipientId(Long id, Long recipientId);

    void deleteByRecipientId(Long recipientId);
}