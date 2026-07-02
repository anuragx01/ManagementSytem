package com.nexstar.portal.notification.repository;

import com.nexstar.portal.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdAndArchivedFalseAndDeletedFalseOrderByCreatedAtDesc(
            UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadFalseAndArchivedFalseAndDeletedFalseOrderByCreatedAtDesc(
            UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndArchivedTrueAndDeletedFalseOrderByCreatedAtDesc(
            UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalseAndArchivedFalseAndDeletedFalse(UUID recipientId);

    Optional<Notification> findByIdAndDeletedFalse(UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :userId AND n.read = false AND n.deleted = false")
    void markAllReadForUser(UUID userId);
}
