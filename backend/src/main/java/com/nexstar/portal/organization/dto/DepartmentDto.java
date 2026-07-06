package com.nexstar.portal.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DepartmentDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Department name is required")
    private String name;

    private String code;
    private String description;
    private UUID parentDepartmentId;
    private String parentDepartmentName;
    private boolean active = true;
}
