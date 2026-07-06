package com.nexstar.portal.organization.dto;

import com.nexstar.portal.organization.entity.Holiday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class HolidayDto {
    private UUID id;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Holiday name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Holiday type is required")
    private Holiday.HolidayType type;

    private String description;
    private boolean optional = false;
}
