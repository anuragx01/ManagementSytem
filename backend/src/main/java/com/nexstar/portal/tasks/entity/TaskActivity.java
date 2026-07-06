package com.nexstar.portal.tasks.entity;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_activities", indexes = {
        @Index(name = "idx_task_activities_task_id", columnList = "task_id"),
        @Index(name = "idx_task_activities_actor_id", columnList = "actor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "from_value", length = 200)
    private String from;

    @Column(name = "to_value", length = 200)
    private String to;

    @Column(columnDefinition = "TEXT")
    private String detail;
}
