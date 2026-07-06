package com.nexstar.portal.leave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LeaveTypeDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Leave type name is required")
    private String name;

    private String code;
    private String description;

    @Min(value = 0, message = "Max days must be non-negative")
    private int maxDaysPerYear;

    private Integer maxConsecutiveDays;
    private boolean paid = true;
    private boolean carryForwardAllowed = false;
    private Integer maxCarryForwardDays;
    private boolean halfDayAllowed = true;
    private boolean documentRequired = false;
    private Integer minAdvanceDays;
    private String color;
    private boolean active = true;
}
