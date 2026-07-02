package com.nexstar.portal.tasks.entity;

import com.nexstar.portal.authentication.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_watchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskWatcher {

    @EmbeddedId
    private TaskWatcherId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "added_at")
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
