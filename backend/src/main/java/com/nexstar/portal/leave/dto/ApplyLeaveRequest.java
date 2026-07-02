package com.nexstar.portal.leave.dto;

import com.nexstar.portal.leave.entity.LeaveRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ApplyLeaveRequest {

    @NotNull(message = "Leave type is required")
    private UUID leaveTypeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private LeaveRequest.DayType dayType = LeaveRequest.DayType.FULL;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String documentUrl;
}
