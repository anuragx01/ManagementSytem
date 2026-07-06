package com.nexstar.portal.leave.dto;

import com.nexstar.portal.leave.entity.LeaveRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LeaveApprovalRequest {

    @NotNull(message = "Leave request ID is required")
    private UUID leaveRequestId;

    @NotNull(message = "Decision is required")
    private LeaveRequest.ApprovalDecision decision;

    private String remarks;
}
