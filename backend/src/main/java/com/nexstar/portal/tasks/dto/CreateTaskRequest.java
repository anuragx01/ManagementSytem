package com.nexstar.portal.tasks.dto;

import com.nexstar.portal.tasks.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID sprintId;

    private UUID parentId;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @Builder.Default
    private Task.TaskType type = Task.TaskType.TASK;

    @Builder.Default
    private Task.TaskPriority priority = Task.TaskPriority.MEDIUM;

    @Builder.Default
    private Task.TaskStatus status = Task.TaskStatus.BACKLOG;

    private UUID assigneeId;

    private UUID reporterId;

    private LocalDate dueDate;

    @Builder.Default
    private int storyPoints = 0;

    private BigDecimal estimatedHours;
}
