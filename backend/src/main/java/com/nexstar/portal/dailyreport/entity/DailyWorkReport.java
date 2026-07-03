package com.nexstar.portal.dailyreport.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_work_reports", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_report_employee_date", columnNames = {"employee_id", "report_date"})
})
public class DailyWorkReport extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "completed_work", nullable = false, columnDefinition = "TEXT")
    private String completedWork;

    @Column(name = "pending_work", columnDefinition = "TEXT")
    private String pendingWork;

    @Column(name = "tomorrow_plan", columnDefinition = "TEXT")
    private String tomorrowPlan;

    @Column(columnDefinition = "TEXT")
    private String blockers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public enum ReportStatus {
        DRAFT, SUBMITTED
    }
}
