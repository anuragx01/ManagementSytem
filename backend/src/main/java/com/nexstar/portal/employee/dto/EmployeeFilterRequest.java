package com.nexstar.portal.employee.dto;

import com.nexstar.portal.employee.entity.Employee;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeFilterRequest {

    private String search;         // name, email, employeeId
    private UUID departmentId;
    private UUID teamId;
    private UUID designationId;
    private UUID branchId;
    private Employee.EmployeeStatus status;
    private Employee.EmploymentType employmentType;
    private String skill;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
