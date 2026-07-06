package com.nexstar.portal.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DesignationDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Designation name is required")
    private String name;

    private String level;
    private String description;
    private boolean active = true;
}
