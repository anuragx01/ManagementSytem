package com.nexstar.portal.timetracking.dto;

import com.nexstar.portal.timetracking.entity.TimeEntry;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TimeEntryResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private UUID projectId;
    private String projectName;
    private UUID taskId;
    private String taskTitle;
    private String taskKey;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String durationFormatted;
    private boolean billable;
    private TimeEntry.TimerStatus timerStatus;
    private LocalDate date;
}
