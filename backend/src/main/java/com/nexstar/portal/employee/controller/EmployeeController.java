package com.nexstar.portal.employee.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.dto.*;
import com.nexstar.portal.employee.entity.*;
import com.nexstar.portal.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.nexstar.portal.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management endpoints")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Employee created successfully", employeeService.createEmployee(request)));
    }

    @GetMapping
    @Operation(summary = "Search and filter employees")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> searchEmployees(
            @ModelAttribute EmployeeFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.searchEmployees(filter)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployee(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's employee profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeByUserId(currentUser.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Update an employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.updateEmployee(id, request)));
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Terminate an employee")
    public ResponseEntity<ApiResponse<Void>> terminateEmployee(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        employeeService.terminateEmployee(id, reason);
        return ResponseEntity.ok(ApiResponse.noContent("Employee terminated"));
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────

    @GetMapping("/{id}/emergency-contacts")
    @Operation(summary = "Get employee emergency contacts")
    public ResponseEntity<ApiResponse<List<EmergencyContact>>> getEmergencyContacts(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmergencyContacts(id)));
    }

    @PostMapping("/{id}/emergency-contacts")
    @Operation(summary = "Add emergency contact")
    public ResponseEntity<ApiResponse<EmergencyContact>> addEmergencyContact(
            @PathVariable UUID id,
            @RequestBody EmergencyContact contact) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Emergency contact added", employeeService.addEmergencyContact(id, contact)));
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    @Operation(summary = "Delete emergency contact")
    public ResponseEntity<ApiResponse<Void>> deleteEmergencyContact(@PathVariable UUID contactId) {
        employeeService.deleteEmergencyContact(contactId);
        return ResponseEntity.ok(ApiResponse.noContent("Emergency contact removed"));
    }

    // ── Skills ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/skills")
    @Operation(summary = "Get employee skills")
    public ResponseEntity<ApiResponse<List<EmployeeSkill>>> getSkills(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getSkills(id)));
    }

    @PostMapping("/{id}/skills")
    @Operation(summary = "Add employee skill")
    public ResponseEntity<ApiResponse<EmployeeSkill>> addSkill(
            @PathVariable UUID id,
            @RequestBody EmployeeSkill skill) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Skill added", employeeService.addSkill(id, skill)));
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(summary = "Remove employee skill")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable UUID skillId) {
        employeeService.deleteSkill(skillId);
        return ResponseEntity.ok(ApiResponse.noContent("Skill removed"));
    }

    // ── Employment History ────────────────────────────────────────────────────

    @GetMapping("/{id}/employment-history")
    @Operation(summary = "Get employment history")
    public ResponseEntity<ApiResponse<List<EmploymentHistory>>> getEmploymentHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmploymentHistory(id)));
    }

    @PostMapping("/{id}/employment-history")
    @Operation(summary = "Add employment history entry")
    public ResponseEntity<ApiResponse<EmploymentHistory>> addEmploymentHistory(
            @PathVariable UUID id,
            @RequestBody EmploymentHistory history) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("History added", employeeService.addEmploymentHistory(id, history)));
    }
}
