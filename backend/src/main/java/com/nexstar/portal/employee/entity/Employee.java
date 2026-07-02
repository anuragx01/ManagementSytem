package com.nexstar.portal.employee.entity;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.organization.entity.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employees_employee_id", columnList = "employee_id"),
        @Index(name = "idx_employees_user_id", columnList = "user_id"),
        @Index(name = "idx_employees_department_id", columnList = "department_id"),
        @Index(name = "idx_employees_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "employee_id", unique = true, length = 20)
    private String employeeId;

    // Personal Info
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 100)
    private String nationality;

    @Column(name = "marital_status", length = 20)
    private String maritalStatus;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "personal_email", length = 100)
    private String personalEmail;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Professional Info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;

    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @Column(name = "date_of_leaving")
    private LocalDate dateOfLeaving;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 20)
    private EmploymentType employmentType;

    @Column(name = "work_location", length = 20)
    private String workLocation; // OFFICE, REMOTE, HYBRID

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE, TERMINATED, RESIGNED, NOTICE_PERIOD
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN, CONSULTANT
    }
}
