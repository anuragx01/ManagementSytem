package com.nexstar.portal.tasks.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.projects.entity.Project;
import com.nexstar.portal.projects.entity.Sprint;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_project_id_status", columnList = "project_id, status"),
        @Index(name = "idx_tasks_sprint_id", columnList = "sprint_id"),
        @Index(name = "idx_tasks_assignee_id", columnList = "assignee_id"),
        @Index(name = "idx_tasks_due_date", columnList = "due_date"),
        @Index(name = "idx_tasks_task_key", columnList = "task_key"),
        @Index(name = "idx_tasks_parent_id", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Task parent;

    @Column(name = "task_key", unique = true, length = 20)
    private String taskKey;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskType type = TaskType.TASK;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.BACKLOG;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Employee assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Employee reporter;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "story_points", nullable = false)
    @Builder.Default
    private int storyPoints = 0;

    @Column(name = "estimated_hours", precision = 8, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 8, scale = 2)
    private BigDecimal actualHours;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private boolean isRecurring = false;

    public enum TaskType {
        EPIC, STORY, TASK, SUBTASK, BUG, IMPROVEMENT
    }

    public enum TaskPriority {
        CRITICAL, HIGH, MEDIUM, LOW
    }

    public enum TaskStatus {
        BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, TESTING, BLOCKED, DONE, CANCELLED
    }
}
