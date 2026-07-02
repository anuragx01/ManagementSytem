package com.nexstar.portal.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceDashboardResponse {

    private LocalDate date;
    private long totalEmployees;
    private long present;
    private long late;
    private long absent;
    private long onLeave;
    private long workFromHome;
    private long weekend;
    private long holiday;

    // Percentages
    private double presentPercentage;
    private double absentPercentage;
    private double latePercentage;
}
