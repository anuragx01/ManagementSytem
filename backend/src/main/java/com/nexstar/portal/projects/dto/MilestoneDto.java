package com.nexstar.portal.projects.dto;

import com.nexstar.portal.projects.entity.Milestone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneDto {

    private UUID id;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Milestone name is required")
    private String name;

    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Milestone.MilestoneStatus status;

    private int completionPercentage;
}
