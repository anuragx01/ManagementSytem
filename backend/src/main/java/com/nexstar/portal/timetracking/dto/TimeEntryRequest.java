package com.nexstar.portal.timetracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TimeEntryRequest {
    private UUID employeeId;
    private UUID projectId;
    private UUID taskId;
    private String description;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private boolean billable = true;
}
