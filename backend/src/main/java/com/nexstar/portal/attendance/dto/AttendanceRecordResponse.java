package com.nexstar.portal.attendance.dto;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AttendanceRecordResponse {

    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private int totalBreakMinutes;
    private int workedMinutes;
    private int lateMinutes;
    private int earlyExitMinutes;
    private int overtimeMinutes;
    private AttendanceRecord.AttendanceStatus status;
    private AttendanceRecord.WorkMode workMode;
    private String clockInLocation;
    private String clockOutLocation;
    private String remarks;
    private boolean regularized;
    private List<BreakDetail> breaks;

    // Computed display fields
    private String workedHours;   // "7h 30m"
    private String lateBy;        // "15 min"
    private String overtimeBy;    // "45 min"

    @Getter
    @Builder
    public static class BreakDetail {
        private UUID id;
        private LocalDateTime breakStart;
        private LocalDateTime breakEnd;
        private Integer durationMinutes;
        private String type;
    }
}
