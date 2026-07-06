package com.nexstar.portal.tasks.dto;

import com.nexstar.portal.tasks.entity.Task;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    private UUID sprintId;
    private UUID parentId;
    private String title;
    private String description;
    private Task.TaskType type;
    private Task.TaskPriority priority;
    private Task.TaskStatus status;
    private UUID assigneeId;
    private UUID reporterId;
    private LocalDate dueDate;
    private Integer storyPoints;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private Integer position;
    private Boolean isRecurring;
}
