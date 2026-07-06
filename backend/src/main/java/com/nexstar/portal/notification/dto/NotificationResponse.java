package com.nexstar.portal.notification.dto;

import com.nexstar.portal.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {

    private UUID id;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private UUID entityId;
    private String entityType;
    private boolean read;
    private boolean archived;
    private LocalDateTime createdAt;
}
