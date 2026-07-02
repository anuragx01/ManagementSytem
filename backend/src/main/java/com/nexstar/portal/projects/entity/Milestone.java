package com.nexstar.portal.projects.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "milestones", indexes = {
        @Index(name = "idx_milestones_project_id", columnList = "project_id"),
        @Index(name = "idx_milestones_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MilestoneStatus status = MilestoneStatus.UPCOMING;

    @Column(name = "completion_percentage", nullable = false)
    @Builder.Default
    private int completionPercentage = 0;

    public enum MilestoneStatus {
        UPCOMING, IN_PROGRESS, COMPLETED, OVERDUE
    }
}
