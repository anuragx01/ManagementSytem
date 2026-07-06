package com.nexstar.portal.tasks.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_checklists", indexes = {
        @Index(name = "idx_task_checklists_task_id", columnList = "task_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskChecklist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;
}
