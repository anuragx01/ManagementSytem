package com.nexstar.portal.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class BranchDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Branch name is required")
    private String name;

    private String code;
    private String address;
    private String city;
    private String state;
    private String country;
    private String timezone;
    private String phone;
    private boolean headquarters = false;
    private boolean active = true;
}
