package com.nexstar.portal.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EmployeeReportResponse {
    private long total;
    private long active;
    private long inactive;
    private Map<String, Long> byDepartment;
    private Map<String, Long> byEmploymentType;
    private long newHires30Days;
    private long terminations30Days;
}
