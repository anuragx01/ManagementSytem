package com.nexstar.portal.leave.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests", indexes = {
        @Index(name = "idx_leave_employee", columnList = "employee_id"),
        @Index(name = "idx_leave_status",   columnList = "status"),
        @Index(name = "idx_leave_dates",    columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days", nullable = false)
    private double totalDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", length = 10)
    @Builder.Default
    private DayType dayType = DayType.FULL;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "document_url")
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    // ── Manager Approval ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_approver_id")
    private Employee managerApprover;

    @Column(name = "manager_action_at")
    private java.time.LocalDateTime managerActionAt;

    @Column(name = "manager_remarks", length = 500)
    private String managerRemarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "manager_decision", length = 20)
    private ApprovalDecision managerDecision;

    // ── HR Approval ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_approver_id")
    private Employee hrApprover;

    @Column(name = "hr_action_at")
    private java.time.LocalDateTime hrActionAt;

    @Column(name = "hr_remarks", length = 500)
    private String hrRemarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "hr_decision", length = 20)
    private ApprovalDecision hrDecision;

    // ── Cancellation ────────────────────────────────────────────────────────

    @Column(name = "cancelled_by_employee", nullable = false)
    @Builder.Default
    private boolean cancelledByEmployee = false;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public enum LeaveStatus {
        PENDING,            // Awaiting manager approval
        MANAGER_APPROVED,   // Manager approved, awaiting HR
        APPROVED,           // Fully approved
        REJECTED,           // Rejected at any step
        CANCELLED,          // Cancelled by employee
        AUTO_APPROVED       // Auto-approved (e.g. WFH)
    }

    public enum DayType {
        FULL, HALF_FIRST, HALF_SECOND
    }

    public enum ApprovalDecision {
        APPROVED, REJECTED
    }
}
