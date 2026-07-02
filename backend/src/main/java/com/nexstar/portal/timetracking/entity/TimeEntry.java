package com.nexstar.portal.timetracking.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.projects.entity.Project;
import com.nexstar.portal.tasks.entity.Task;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_entries", indexes = {
        @Index(name = "idx_te_employee_date", columnList = "employee_id, date"),
        @Index(name = "idx_te_project",       columnList = "project_id"),
        @Index(name = "idx_te_task",          columnList = "task_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(length = 500)
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(nullable = false)
    @Builder.Default
    private boolean billable = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "timer_status", nullable = false, length = 10)
    @Builder.Default
    private TimerStatus timerStatus = TimerStatus.STOPPED;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "total_paused_minutes", nullable = false)
    @Builder.Default
    private int totalPausedMinutes = 0;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "timesheet_id")
    private UUID timesheetId;

    public enum TimerStatus {
        RUNNING, PAUSED, STOPPED
    }
}
