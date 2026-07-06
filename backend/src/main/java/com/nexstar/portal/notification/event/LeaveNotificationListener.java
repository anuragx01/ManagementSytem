package com.nexstar.portal.notification.event;

import com.nexstar.portal.leave.entity.LeaveRequest;
import com.nexstar.portal.notification.entity.Notification;
import com.nexstar.portal.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveNotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleLeaveEvent(LeaveNotificationEvent event) {
        LeaveRequest lr = event.getLeaveRequest();
        String empName = lr.getEmployee().getUser() != null
                ? lr.getEmployee().getUser().getFirstName() + " " + lr.getEmployee().getUser().getLastName()
                : lr.getEmployee().getEmployeeId();
        String leaveName = lr.getLeaveType().getName();
        String actionUrl = "/leaves/" + lr.getId();

        try {
            switch (event.getAction()) {
                case APPLIED -> {
                    // Notify manager
                    if (lr.getEmployee().getReportingManager() != null
                            && lr.getEmployee().getReportingManager().getUser() != null) {
                        notificationService.createAndPush(
                                lr.getEmployee().getReportingManager().getUser().getId(),
                                Notification.NotificationType.LEAVE_APPLIED,
                                "New Leave Request",
                                empName + " has applied for " + leaveName
                                        + " from " + lr.getStartDate() + " to " + lr.getEndDate(),
                                actionUrl, lr.getId(), "LEAVE_REQUEST"
                        );
                    }
                }
                case MANAGER_APPROVED -> {
                    // Notify employee — waiting for HR
                    notificationService.createAndPush(
                            lr.getEmployee().getUser().getId(),
                            Notification.NotificationType.LEAVE_APPROVED,
                            "Leave Pending HR Approval",
                            "Your " + leaveName + " request has been approved by your manager and is now with HR.",
                            actionUrl, lr.getId(), "LEAVE_REQUEST"
                    );
                }
                case MANAGER_REJECTED -> {
                    notificationService.createAndPush(
                            lr.getEmployee().getUser().getId(),
                            Notification.NotificationType.LEAVE_REJECTED,
                            "Leave Request Rejected",
                            "Your " + leaveName + " request was rejected by your manager."
                                    + (lr.getManagerRemarks() != null ? " Reason: " + lr.getManagerRemarks() : ""),
                            actionUrl, lr.getId(), "LEAVE_REQUEST"
                    );
                }
                case HR_APPROVED -> {
                    notificationService.createAndPush(
                            lr.getEmployee().getUser().getId(),
                            Notification.NotificationType.LEAVE_APPROVED,
                            "Leave Approved",
                            "Your " + leaveName + " request from " + lr.getStartDate()
                                    + " to " + lr.getEndDate() + " has been approved.",
                            actionUrl, lr.getId(), "LEAVE_REQUEST"
                    );
                }
                case HR_REJECTED -> {
                    notificationService.createAndPush(
                            lr.getEmployee().getUser().getId(),
                            Notification.NotificationType.LEAVE_REJECTED,
                            "Leave Request Rejected",
                            "Your " + leaveName + " request was rejected by HR."
                                    + (lr.getHrRemarks() != null ? " Reason: " + lr.getHrRemarks() : ""),
                            actionUrl, lr.getId(), "LEAVE_REQUEST"
                    );
                }
                case CANCELLED -> {
                    // Notify manager if it was already approved
                    if (lr.getEmployee().getReportingManager() != null
                            && lr.getEmployee().getReportingManager().getUser() != null) {
                        notificationService.createAndPush(
                                lr.getEmployee().getReportingManager().getUser().getId(),
                                Notification.NotificationType.LEAVE_CANCELLED,
                                "Leave Cancelled",
                                empName + " has cancelled their " + leaveName + " leave.",
                                actionUrl, lr.getId(), "LEAVE_REQUEST"
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send leave notification for event {}: {}", event.getAction(), e.getMessage());
        }
    }
}
