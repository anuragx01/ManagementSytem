package com.nexstar.portal.dailyreport.dto;

import com.nexstar.portal.dailyreport.entity.DailyWorkReport;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyWorkReportDto {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private LocalDate reportDate;
    @NotBlank
    private String completedWork;
    private String pendingWork;
    private String tomorrowPlan;
    private String blockers;
    private DailyWorkReport.ReportStatus status;
    private LocalDateTime submittedAt;
}
