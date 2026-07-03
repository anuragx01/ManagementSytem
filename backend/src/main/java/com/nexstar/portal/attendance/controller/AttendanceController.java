package com.nexstar.portal.attendance.controller;

import com.nexstar.portal.attendance.dto.*;
import com.nexstar.portal.attendance.service.AttendanceService;
import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance management endpoints")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── Self-service ──────────────────────────────────────────────────────────

    @PostMapping("/clock-in")
    @Operation(summary = "Clock in for the day")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> clockIn(
            @RequestBody(required = false) ClockInRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (request == null) request = new ClockInRequest();
        return ResponseEntity.ok(ApiResponse.success("Clocked in successfully",
                attendanceService.clockIn(request, currentUser)));
    }

    @PostMapping("/clock-out")
    @Operation(summary = "Clock out for the day")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> clockOut(
            @RequestBody(required = false) ClockOutRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (request == null) request = new ClockOutRequest();
        return ResponseEntity.ok(ApiResponse.success("Clocked out successfully",
                attendanceService.clockOut(request, currentUser)));
    }

    @PostMapping("/break/start")
    @Operation(summary = "Start a break")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> startBreak(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Break started",
                attendanceService.startBreak(currentUser)));
    }

    @PostMapping("/break/end")
    @Operation(summary = "End a break and resume work")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> endBreak(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Break ended",
                attendanceService.endBreak(currentUser)));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's attendance status for logged-in user")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> getTodayStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayStatus(currentUser)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my attendance history")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceRecordResponse>>> getMyAttendance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getMyAttendance(currentUser, page, size)));
    }

    // ── HR / Manager View ─────────────────────────────────────────────────────

    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Get all employee attendance records")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceRecordResponse>>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAllAttendance(page, size)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Get attendance records for a specific employee")
    public ResponseEntity<ApiResponse<List<AttendanceRecordResponse>>> getEmployeeAttendance(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getEmployeeAttendance(employeeId, from, to, currentUser)));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    @Operation(summary = "Get attendance dashboard summary for a date")
    public ResponseEntity<ApiResponse<AttendanceDashboardResponse>> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getDashboard(date)));
    }

    @PostMapping("/regularize")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Regularize (override) an attendance record")
    public ResponseEntity<ApiResponse<AttendanceRecordResponse>> regularize(
            @Valid @RequestBody RegularizeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance regularized",
                attendanceService.regularize(request)));
    }

    // ── Policies ──────────────────────────────────────────────────────────────

    @GetMapping("/policies")
    @Operation(summary = "Get attendance policies for a company")
    public ResponseEntity<ApiResponse<List<AttendancePolicyDto>>> getPolicies(
            @RequestParam UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getPolicies(companyId)));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    @Operation(summary = "Create an attendance policy")
    public ResponseEntity<ApiResponse<AttendancePolicyDto>> createPolicy(
            @Valid @RequestBody AttendancePolicyDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Policy created",
                attendanceService.createPolicy(dto)));
    }
}
