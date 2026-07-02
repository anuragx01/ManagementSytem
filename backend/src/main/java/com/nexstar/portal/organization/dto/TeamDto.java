package com.nexstar.portal.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TeamDto {
    private UUID id;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    private String departmentName;

    @NotBlank(message = "Team name is required")
    private String name;

    private String code;
    private String description;
    private boolean active = true;
}
