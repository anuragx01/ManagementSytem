package com.nexstar.portal.employee.dto;

import com.nexstar.portal.employee.entity.Employee;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EmployeeResponse {

    private UUID id;
    private String employeeId;

    // User info
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePictureUrl;

    // Personal
    private LocalDate dateOfBirth;
    private Employee.Gender gender;
    private String nationality;
    private String maritalStatus;
    private String bloodGroup;
    private String personalEmail;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;

    // Professional
    private UUID departmentId;
    private String departmentName;
    private UUID teamId;
    private String teamName;
    private UUID designationId;
    private String designationName;
    private UUID branchId;
    private String branchName;
    private UUID reportingManagerId;
    private String reportingManagerName;
    private LocalDate dateOfJoining;
    private Employee.EmployeeStatus status;
    private Employee.EmploymentType employmentType;
    private String workLocation;

    private LocalDateTime createdAt;
}
