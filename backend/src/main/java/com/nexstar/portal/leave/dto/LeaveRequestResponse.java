package com.nexstar.portal.leave.dto;

import com.nexstar.portal.leave.entity.LeaveRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LeaveRequestResponse {

    private UUID id;

    // Employee
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String departmentName;

    // Leave type
    private UUID leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeColor;
    private boolean paid;

    // Dates
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalDays;
    private LeaveRequest.DayType dayType;

    // Content
    private String reason;
    private String documentUrl;

    // Status
    private LeaveRequest.LeaveStatus status;

    // Manager approval
    private UUID managerId;
    private String managerName;
    private LeaveRequest.ApprovalDecision managerDecision;
    private String managerRemarks;
    private LocalDateTime managerActionAt;

    // HR approval
    private UUID hrId;
    private String hrName;
    private LeaveRequest.ApprovalDecision hrDecision;
    private String hrRemarks;
    private LocalDateTime hrActionAt;

    // Meta
    private LocalDateTime appliedAt;
}
