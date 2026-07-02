package com.nexstar.portal.projects.dto;

import com.nexstar.portal.projects.entity.Sprint;
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
public class SprintDto {

    private UUID id;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID milestoneId;

    @NotBlank(message = "Sprint name is required")
    private String name;

    private String goal;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Sprint.SprintStatus status;

    private int velocity;
}
