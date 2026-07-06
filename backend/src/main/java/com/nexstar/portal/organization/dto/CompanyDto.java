package com.nexstar.portal.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CompanyDto {
    private UUID id;

    @NotBlank(message = "Company name is required")
    private String name;

    private String logoUrl;
    private String industry;
    private String website;
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String timezone;
    private String currency;
    private String dateFormat;
    private String description;
}
