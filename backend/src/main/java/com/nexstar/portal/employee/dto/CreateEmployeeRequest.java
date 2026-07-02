package com.nexstar.portal.employee.dto;

import com.nexstar.portal.employee.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateEmployeeRequest {

    // User account fields
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Personal info
    private LocalDate dateOfBirth;
    private Employee.Gender gender;
    private String nationality;
    private String maritalStatus;
    private String bloodGroup;
    private String personalEmail;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    private String alternatePhone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // Professional info
    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    private UUID departmentId;
    private UUID teamId;
    private UUID designationId;
    private UUID branchId;
    private UUID reportingManagerId;
    private Employee.EmploymentType employmentType;
    private String workLocation;

    // Custom employee ID (optional, auto-generated if blank)
    private String employeeId;
}
