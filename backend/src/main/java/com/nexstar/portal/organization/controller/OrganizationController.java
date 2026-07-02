package com.nexstar.portal.organization.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.organization.dto.*;
import com.nexstar.portal.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Organization management endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;

    // ── Company ───────────────────────────────────────────────────────────────

    @GetMapping("/company")
    @Operation(summary = "Get company profile")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompany() {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getCompany()));
    }

    @PutMapping("/company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create or update company profile")
    public ResponseEntity<ApiResponse<CompanyDto>> upsertCompany(@Valid @RequestBody CompanyDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Company profile updated", organizationService.upsertCompany(dto)));
    }

    // ── Departments ───────────────────────────────────────────────────────────

    @GetMapping("/departments")
    @Operation(summary = "Get all departments for a company")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartments(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getDepartments(companyId)));
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create a department")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(@Valid @RequestBody DepartmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Department created", organizationService.createDepartment(dto)));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(
            @PathVariable UUID id, @Valid @RequestBody DepartmentDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Department updated", organizationService.updateDepartment(id, dto)));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a department")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        organizationService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.noContent("Department deleted"));
    }

    // ── Teams ─────────────────────────────────────────────────────────────────

    @GetMapping("/departments/{departmentId}/teams")
    @Operation(summary = "Get teams in a department")
    public ResponseEntity<ApiResponse<List<TeamDto>>> getTeams(@PathVariable UUID departmentId) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getTeamsByDepartment(departmentId)));
    }

    @PostMapping("/teams")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Create a team")
    public ResponseEntity<ApiResponse<TeamDto>> createTeam(@Valid @RequestBody TeamDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Team created", organizationService.createTeam(dto)));
    }

    @PutMapping("/teams/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Update a team")
    public ResponseEntity<ApiResponse<TeamDto>> updateTeam(@PathVariable UUID id, @Valid @RequestBody TeamDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Team updated", organizationService.updateTeam(id, dto)));
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Delete a team")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable UUID id) {
        organizationService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.noContent("Team deleted"));
    }

    // ── Designations ──────────────────────────────────────────────────────────

    @GetMapping("/designations")
    @Operation(summary = "Get all designations")
    public ResponseEntity<ApiResponse<List<DesignationDto>>> getDesignations(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getDesignations(companyId)));
    }

    @PostMapping("/designations")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create a designation")
    public ResponseEntity<ApiResponse<DesignationDto>> createDesignation(@Valid @RequestBody DesignationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Designation created", organizationService.createDesignation(dto)));
    }

    @PutMapping("/designations/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Update a designation")
    public ResponseEntity<ApiResponse<DesignationDto>> updateDesignation(
            @PathVariable UUID id, @Valid @RequestBody DesignationDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Designation updated", organizationService.updateDesignation(id, dto)));
    }

    @DeleteMapping("/designations/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a designation")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable UUID id) {
        organizationService.deleteDesignation(id);
        return ResponseEntity.ok(ApiResponse.noContent("Designation deleted"));
    }

    // ── Branches ──────────────────────────────────────────────────────────────

    @GetMapping("/branches")
    @Operation(summary = "Get all branches")
    public ResponseEntity<ApiResponse<List<BranchDto>>> getBranches(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getBranches(companyId)));
    }

    @PostMapping("/branches")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a branch")
    public ResponseEntity<ApiResponse<BranchDto>> createBranch(@Valid @RequestBody BranchDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Branch created", organizationService.createBranch(dto)));
    }

    @PutMapping("/branches/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a branch")
    public ResponseEntity<ApiResponse<BranchDto>> updateBranch(@PathVariable UUID id, @Valid @RequestBody BranchDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Branch updated", organizationService.updateBranch(id, dto)));
    }

    @DeleteMapping("/branches/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a branch")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable UUID id) {
        organizationService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.noContent("Branch deleted"));
    }

    // ── Holidays ──────────────────────────────────────────────────────────────

    @GetMapping("/holidays")
    @Operation(summary = "Get all holidays")
    public ResponseEntity<ApiResponse<List<HolidayDto>>> getHolidays(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(organizationService.getHolidays(companyId)));
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create a holiday")
    public ResponseEntity<ApiResponse<HolidayDto>> createHoliday(@Valid @RequestBody HolidayDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Holiday created", organizationService.createHoliday(dto)));
    }

    @PutMapping("/holidays/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Update a holiday")
    public ResponseEntity<ApiResponse<HolidayDto>> updateHoliday(@PathVariable UUID id, @Valid @RequestBody HolidayDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Holiday updated", organizationService.updateHoliday(id, dto)));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Delete a holiday")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable UUID id) {
        organizationService.deleteHoliday(id);
        return ResponseEntity.ok(ApiResponse.noContent("Holiday deleted"));
    }
}
