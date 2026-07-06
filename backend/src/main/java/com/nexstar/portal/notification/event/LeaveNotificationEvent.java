package com.nexstar.portal.notification.event;

import com.nexstar.portal.leave.entity.LeaveRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LeaveNotificationEvent extends ApplicationEvent {

    private final LeaveRequest leaveRequest;
    private final EventAction action;

    public LeaveNotificationEvent(Object source, LeaveRequest leaveRequest, EventAction action) {
        super(source);
        this.leaveRequest = leaveRequest;
        this.action = action;
    }

    public enum EventAction {
        APPLIED,
        MANAGER_APPROVED,
        MANAGER_REJECTED,
        HR_APPROVED,
        HR_REJECTED,
        CANCELLED
    }
}
