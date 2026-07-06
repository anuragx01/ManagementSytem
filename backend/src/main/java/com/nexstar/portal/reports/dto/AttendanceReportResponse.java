package com.nexstar.portal.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AttendanceReportResponse {
    private LocalDate from;
    private LocalDate to;
    private long totalRecords;
    private long presentCount;
    private long absentCount;
    private long lateCount;
    private List<EmployeeAttendanceSummary> employees;

    @Data
    @Builder
    public static class EmployeeAttendanceSummary {
        private UUID employeeId;
        private String name;
        private int presentDays;
        private int absentDays;
        private int lateDays;
        private int workedMinutes;
    }
}
