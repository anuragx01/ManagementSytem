package com.nexstar.portal.notification.service;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.notification.dto.NotificationResponse;
import com.nexstar.portal.notification.entity.Notification;
import com.nexstar.portal.notification.repository.NotificationRepository;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Create & Push ─────────────────────────────────────────────────────────

    /**
     * Create a notification for a user and push it via WebSocket.
     */
    @Transactional
    public NotificationResponse createAndPush(
            UUID recipientUserId,
            Notification.NotificationType type,
            String title,
            String message,
            String actionUrl,
            UUID entityId,
            String entityType) {

        User recipient = userRepository.findByIdAndDeletedFalse(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recipientUserId));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .entityId(entityId)
                .entityType(entityType)
                .build();

        notification = notificationRepository.save(notification);
        NotificationResponse response = toResponse(notification);

        // Push via WebSocket to the specific user's queue
        try {
            messagingTemplate.convertAndSendToUser(
                    recipientUserId.toString(),
                    "/queue/notifications",
                    response
            );
        } catch (Exception e) {
            log.warn("WebSocket push failed for user {}: {}", recipientUserId, e.getMessage());
        }

        return response;
    }

    /** Convenience overload without entityId/entityType */
    @Transactional
    public NotificationResponse createAndPush(
            UUID recipientUserId,
            Notification.NotificationType type,
            String title,
            String message) {
        return createAndPush(recipientUserId, type, title, message, null, null, null);
    }

    // ── Center (inbox) ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getInbox(UserPrincipal currentUser, int page, int size) {
        var result = notificationRepository
                .findByRecipientIdAndArchivedFalseAndDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(), PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUnread(UserPrincipal currentUser, int page, int size) {
        var result = notificationRepository
                .findByRecipientIdAndReadFalseAndArchivedFalseAndDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(), PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getArchived(UserPrincipal currentUser, int page, int size) {
        var result = notificationRepository
                .findByRecipientIdAndArchivedTrueAndDeletedFalseOrderByCreatedAtDesc(
                        currentUser.getId(), PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal currentUser) {
        return notificationRepository.countByRecipientIdAndReadFalseAndArchivedFalseAndDeletedFalse(
                currentUser.getId());
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Transactional
    public NotificationResponse markRead(UUID id, UserPrincipal currentUser) {
        Notification n = getOwned(id, currentUser);
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void markAllRead(UserPrincipal currentUser) {
        notificationRepository.markAllReadForUser(currentUser.getId());
    }

    @Transactional
    public NotificationResponse archive(UUID id, UserPrincipal currentUser) {
        Notification n = getOwned(id, currentUser);
        n.setArchived(true);
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void delete(UUID id, UserPrincipal currentUser) {
        Notification n = getOwned(id, currentUser);
        n.setDeleted(true);
        notificationRepository.save(n);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Notification getOwned(UUID id, UserPrincipal currentUser) {
        Notification n = notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (!n.getRecipient().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }
        return n;
    }

    NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .actionUrl(n.getActionUrl())
                .entityId(n.getEntityId())
                .entityType(n.getEntityType())
                .read(n.isRead())
                .archived(n.isArchived())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
