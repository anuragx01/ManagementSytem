package com.nexstar.portal.timetracking.dto;

import com.nexstar.portal.timetracking.entity.Timesheet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TimesheetResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private int totalMinutes;
    private int billableMinutes;
    private String totalFormatted;
    private String billableFormatted;
    private Timesheet.TimesheetStatus status;
    private String approverName;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private List<TimeEntryResponse> entries;
}
