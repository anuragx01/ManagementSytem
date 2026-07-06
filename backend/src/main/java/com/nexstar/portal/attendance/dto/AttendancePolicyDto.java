package com.nexstar.portal.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class AttendancePolicyDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotNull(message = "Office start time is required")
    private LocalTime officeStartTime;

    @NotNull(message = "Office end time is required")
    private LocalTime officeEndTime;

    private int graceTimeMinutes = 15;
    private int halfDayHours = 4;
    private int fullDayHours = 8;
    private int overtimeThresholdMinutes = 30;
    private int workingDaysMask = 62;
    private boolean allowWorkFromHome = true;
    private boolean requireLocation = false;
    private boolean isDefault = false;
    private boolean active = true;
}
