package com.nexstar.portal.attendance.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", indexes = {
        @Index(name = "idx_att_employee_date", columnList = "employee_id, date"),
        @Index(name = "idx_att_date",          columnList = "date"),
        @Index(name = "idx_att_status",        columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    /** Total break minutes taken during the day */
    @Column(name = "total_break_minutes", nullable = false)
    @Builder.Default
    private int totalBreakMinutes = 0;

    /** Net worked minutes = (clockOut - clockIn) - totalBreakMinutes */
    @Column(name = "worked_minutes")
    private Integer workedMinutes;

    /** Minutes late from office start (0 if on time) */
    @Column(name = "late_minutes", nullable = false)
    @Builder.Default
    private int lateMinutes = 0;

    /** Minutes left early (0 if on time or after end) */
    @Column(name = "early_exit_minutes", nullable = false)
    @Builder.Default
    private int earlyExitMinutes = 0;

    /** Overtime minutes beyond full-day threshold */
    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private int overtimeMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 10)
    private WorkMode workMode;

    @Column(name = "clock_in_location", length = 200)
    private String clockInLocation;

    @Column(name = "clock_out_location", length = 200)
    private String clockOutLocation;

    /** HR or manager override note */
    @Column(length = 500)
    private String remarks;

    /** Whether this record was manually adjusted by HR/admin */
    @Column(name = "is_regularized", nullable = false)
    @Builder.Default
    private boolean regularized = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id")
    private AttendancePolicy policy;

    public enum AttendanceStatus {
        PRESENT,        // On time, full day
        LATE,           // Arrived after grace period
        HALF_DAY,       // Worked half the expected hours
        ABSENT,         // No clock-in
        ON_LEAVE,       // Covered by approved leave
        HOLIDAY,        // Public/company holiday
        WEEKEND,        // Non-working day
        WORK_FROM_HOME  // WFH day
    }

    public enum WorkMode {
        OFFICE, WFH, HYBRID
    }
}
