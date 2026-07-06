package com.nexstar.portal.timetracking.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "timesheets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_timesheet_employee_week", columnNames = {"employee_id", "week_start_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Timesheet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "total_minutes", nullable = false)
    @Builder.Default
    private int totalMinutes = 0;

    @Column(name = "billable_minutes", nullable = false)
    @Builder.Default
    private int billableMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TimesheetStatus status = TimesheetStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approver_remarks", length = 500)
    private String approverRemarks;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    public enum TimesheetStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED
    }
}
