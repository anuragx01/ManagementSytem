package com.nexstar.portal.projects.dto;

import com.nexstar.portal.projects.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be between 2 and 10 characters")
    private String key;

    private String description;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private Project.ProjectType type;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isPrivate;
}
