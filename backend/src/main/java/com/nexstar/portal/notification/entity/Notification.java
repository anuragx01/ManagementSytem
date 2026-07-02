package com.nexstar.portal.notification.entity;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notif_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notif_read",      columnList = "is_read"),
        @Index(name = "idx_notif_type",      columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Deep-link for the frontend (e.g. /leaves/123) */
    @Column(length = 500)
    private String actionUrl;

    /** The entity this notification relates to (e.g. leave request UUID, task UUID) */
    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "entity_type", length = 30)
    private String entityType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private boolean archived = false;

    public enum NotificationType {
        TASK_ASSIGNED,
        TASK_UPDATED,
        TASK_COMMENT,
        TASK_MENTIONED,
        TASK_DEADLINE,
        LEAVE_APPLIED,
        LEAVE_APPROVED,
        LEAVE_REJECTED,
        LEAVE_CANCELLED,
        ATTENDANCE_REMINDER,
        ANNOUNCEMENT,
        SYSTEM
    }
}
