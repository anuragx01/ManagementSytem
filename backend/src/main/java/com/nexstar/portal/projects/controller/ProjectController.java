package com.nexstar.portal.projects.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.projects.dto.*;
import com.nexstar.portal.projects.entity.ProjectMember;
import com.nexstar.portal.projects.service.ProjectService;
import com.nexstar.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management, members, milestones and sprints")
public class ProjectController {

    private final ProjectService projectService;

    // ── Projects ──────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Project created", projectService.createProject(request, currentUser)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(id)));
    }

    @GetMapping
    @Operation(summary = "Get all projects for a company")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getProjects(
            @RequestParam UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjects(companyId, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Update project")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request)));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Archive a project")
    public ResponseEntity<ApiResponse<Void>> archiveProject(@PathVariable UUID id) {
        projectService.archiveProject(id);
        return ResponseEntity.ok(ApiResponse.noContent("Project archived"));
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @GetMapping("/{projectId}/members")
    @Operation(summary = "Get all members of a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberDto>>> getMembers(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getMembers(projectId)));
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Add a member to a project")
    public ResponseEntity<ApiResponse<ProjectMemberDto>> addMember(
            @PathVariable UUID projectId,
            @RequestParam UUID employeeId,
            @RequestParam(defaultValue = "DEVELOPER") ProjectMember.MemberRole role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Member added", projectService.addMember(projectId, employeeId, role)));
    }

    @DeleteMapping("/{projectId}/members/{employeeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Remove a member from a project")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID employeeId) {
        projectService.removeMember(projectId, employeeId);
        return ResponseEntity.ok(ApiResponse.noContent("Member removed"));
    }

    // ── Milestones ────────────────────────────────────────────────────────────

    @PostMapping("/{projectId}/milestones")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Create a milestone")
    public ResponseEntity<ApiResponse<MilestoneDto>> createMilestone(
            @PathVariable UUID projectId,
            @Valid @RequestBody MilestoneDto dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Milestone created", projectService.createMilestone(dto)));
    }

    @GetMapping("/{projectId}/milestones")
    @Operation(summary = "Get all milestones of a project")
    public ResponseEntity<ApiResponse<List<MilestoneDto>>> getMilestones(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getMilestones(projectId)));
    }

    @PutMapping("/{projectId}/milestones/{milestoneId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Update a milestone")
    public ResponseEntity<ApiResponse<MilestoneDto>> updateMilestone(
            @PathVariable UUID projectId,
            @PathVariable UUID milestoneId,
            @Valid @RequestBody MilestoneDto dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success("Milestone updated", projectService.updateMilestone(milestoneId, dto)));
    }

    // ── Sprints ───────────────────────────────────────────────────────────────

    @PostMapping("/{projectId}/sprints")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Create a sprint")
    public ResponseEntity<ApiResponse<SprintDto>> createSprint(
            @PathVariable UUID projectId,
            @Valid @RequestBody SprintDto dto) {
        dto.setProjectId(projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Sprint created", projectService.createSprint(dto)));
    }

    @GetMapping("/{projectId}/sprints")
    @Operation(summary = "Get all sprints of a project")
    public ResponseEntity<ApiResponse<List<SprintDto>>> getSprints(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getSprints(projectId)));
    }

    @GetMapping("/{projectId}/sprints/active")
    @Operation(summary = "Get the active sprint of a project")
    public ResponseEntity<ApiResponse<SprintDto>> getActiveSprint(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getActiveSprint(projectId)));
    }

    @PostMapping("/sprints/{sprintId}/start")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Start a sprint")
    public ResponseEntity<ApiResponse<SprintDto>> startSprint(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success("Sprint started", projectService.startSprint(sprintId)));
    }

    @PostMapping("/sprints/{sprintId}/complete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Complete a sprint")
    public ResponseEntity<ApiResponse<SprintDto>> completeSprint(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success("Sprint completed", projectService.completeSprint(sprintId)));
    }
}
