package com.nexstar.portal.tasks.dto;

import com.nexstar.portal.tasks.entity.Task;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;
    private UUID projectId;
    private String projectName;
    private String projectKey;
    private UUID sprintId;
    private String sprintName;
    private UUID parentId;
    private String parentTaskKey;
    private String taskKey;
    private String title;
    private String description;
    private Task.TaskType type;
    private Task.TaskPriority priority;
    private Task.TaskStatus status;
    private UUID assigneeId;
    private String assigneeName;
    private UUID reporterId;
    private String reporterName;
    private LocalDate dueDate;
    private int storyPoints;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private int position;
    private boolean isRecurring;
    private long commentCount;
    private int checklistTotal;
    private int checklistDone;
    private long watcherCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
