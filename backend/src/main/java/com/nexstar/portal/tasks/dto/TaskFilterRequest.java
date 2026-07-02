package com.nexstar.portal.tasks.dto;

import com.nexstar.portal.tasks.entity.Task;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskFilterRequest {

    private UUID projectId;
    private UUID sprintId;
    private UUID assigneeId;
    private Task.TaskType type;
    private Task.TaskPriority priority;
    private Task.TaskStatus status;
    private String search;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}
