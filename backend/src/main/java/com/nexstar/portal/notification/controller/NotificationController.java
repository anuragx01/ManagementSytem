package com.nexstar.portal.notification.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.notification.dto.NotificationResponse;
import com.nexstar.portal.notification.service.NotificationService;
import com.nexstar.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification center — inbox, read, archive")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notification inbox (all non-archived)")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getInbox(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getInbox(currentUser, page, size)));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUnread(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnread(currentUser, page, size)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getUnreadCount(currentUser)));
    }

    @GetMapping("/archived")
    @Operation(summary = "Get archived notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getArchived(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.getArchived(currentUser, page, size)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.markRead(id, currentUser)));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllRead(currentUser);
        return ResponseEntity.ok(ApiResponse.noContent("All notifications marked as read"));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive a notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> archive(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.archive(id, currentUser)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.noContent("Notification deleted"));
    }
}
