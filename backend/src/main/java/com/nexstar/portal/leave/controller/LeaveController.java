package com.nexstar.portal.leave.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.leave.dto.*;
import com.nexstar.portal.leave.service.LeaveService;
import com.nexstar.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Leave types, applications and approval workflow")
public class LeaveController {

    private final LeaveService leaveService;

    // ── Leave Types ───────────────────────────────────────────────────────────

    @GetMapping("/types")
    @Operation(summary = "Get all leave types for a company")
    public ResponseEntity<ApiResponse<List<LeaveTypeDto>>> getLeaveTypes(@RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveTypes(companyId)));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create a leave type")
    public ResponseEntity<ApiResponse<LeaveTypeDto>> createLeaveType(@Valid @RequestBody LeaveTypeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Leave type created", leaveService.createLeaveType(dto)));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Update a leave type")
    public ResponseEntity<ApiResponse<LeaveTypeDto>> updateLeaveType(
            @PathVariable UUID id, @Valid @RequestBody LeaveTypeDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Leave type updated", leaveService.updateLeaveType(id, dto)));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a leave type")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(@PathVariable UUID id) {
        leaveService.deleteLeaveType(id);
        return ResponseEntity.ok(ApiResponse.noContent("Leave type deleted"));
    }

    // ── Leave Balance ─────────────────────────────────────────────────────────

    @GetMapping("/balance/my")
    @Operation(summary = "Get my leave balances for current year")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalance(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getMyBalance(currentUser)));
    }

    @GetMapping("/balance/employee/{employeeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Get leave balances for a specific employee")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getEmployeeBalance(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int year) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getEmployeeBalance(employeeId, year)));
    }

    @PostMapping("/balance/allocate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Allocate leave days to an employee")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> allocateLeave(
            @RequestParam UUID employeeId,
            @RequestParam UUID leaveTypeId,
            @RequestParam int year,
            @RequestParam double days) {
        return ResponseEntity.ok(ApiResponse.success("Leave allocated",
                leaveService.allocateLeave(employeeId, leaveTypeId, year, days)));
    }

    // ── Apply Leave ───────────────────────────────────────────────────────────

    @PostMapping("/apply")
    @Operation(summary = "Apply for leave")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> applyLeave(
            @Valid @RequestBody ApplyLeaveRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Leave request submitted",
                        leaveService.applyLeave(request, currentUser)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my leave requests")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getMyLeaves(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getMyLeaves(currentUser, page, size)));
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "Cancel a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> cancelLeave(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Leave cancelled",
                leaveService.cancelLeave(id, reason, currentUser)));
    }

    // ── Manager Approval ──────────────────────────────────────────────────────

    @GetMapping("/pending/manager")
    @PreAuthorize("hasRole('MANAGER') or hasRole('TEAM_LEAD')")
    @Operation(summary = "Get leave requests pending manager approval")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getPendingForManager(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getPendingForManager(currentUser)));
    }

    @PostMapping("/approve/manager")
    @PreAuthorize("hasRole('MANAGER') or hasRole('TEAM_LEAD')")
    @Operation(summary = "Manager approves or rejects a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> managerDecision(
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Decision recorded",
                leaveService.managerDecision(request, currentUser)));
    }

    // ── HR Approval ───────────────────────────────────────────────────────────

    @GetMapping("/pending/hr")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Get leave requests pending HR approval")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getPendingForHr() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getPendingForHr()));
    }

    @PostMapping("/approve/hr")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "HR approves or rejects a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> hrDecision(
            @Valid @RequestBody LeaveApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Decision recorded",
                leaveService.hrDecision(request, currentUser)));
    }

    // ── Calendar ──────────────────────────────────────────────────────────────

    @GetMapping("/calendar")
    @Operation(summary = "Get approved leaves for a date range (company-wide calendar)")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getLeaveCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveCalendar(from, to)));
    }

    @GetMapping("/calendar/team/{teamId}")
    @Operation(summary = "Get approved leaves for a specific team")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getTeamLeaveCalendar(
            @PathVariable UUID teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getTeamLeaveCalendar(teamId, from, to)));
    }
}
