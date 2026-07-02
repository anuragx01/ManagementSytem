package com.nexstar.portal.leave.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LeaveBalanceResponse {

    private UUID id;
    private UUID leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeColor;
    private boolean paid;
    private int year;

    private double allocatedDays;
    private double carriedForwardDays;
    private double usedDays;
    private double pendingDays;
    private double availableDays;
}
