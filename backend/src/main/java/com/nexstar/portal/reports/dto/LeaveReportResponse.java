package com.nexstar.portal.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LeaveReportResponse {
    private LocalDate from;
    private LocalDate to;
    private List<LeaveTypeSummary> byType;
    private List<EmployeeLeaveSummary> employees;

    @Data
    @Builder
    public static class LeaveTypeSummary {
        private String leaveType;
        private long total;
        private long approved;
        private long rejected;
        private long pending;
        private double totalDays;
    }

    @Data
    @Builder
    public static class EmployeeLeaveSummary {
        private UUID employeeId;
        private String name;
        private double totalDays;
        private long requests;
    }
}
