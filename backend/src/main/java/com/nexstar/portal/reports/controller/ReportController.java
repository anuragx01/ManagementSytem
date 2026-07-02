package com.nexstar.portal.reports.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.reports.dto.*;
import com.nexstar.portal.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytics and reporting endpoints")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/attendance")
    @Operation(summary = "Get attendance report for a date range")
    public ResponseEntity<ApiResponse<AttendanceReportResponse>> getAttendanceReport(
            @RequestParam(required = false) UUID companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getAttendanceReport(companyId, from, to)));
    }

    @GetMapping("/leave")
    @Operation(summary = "Get leave report for a date range")
    public ResponseEntity<ApiResponse<LeaveReportResponse>> getLeaveReport(
            @RequestParam(required = false) UUID companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getLeaveReport(companyId, from, to)));
    }

    @GetMapping("/employees")
    @Operation(summary = "Get employee headcount report")
    public ResponseEntity<ApiResponse<EmployeeReportResponse>> getEmployeeReport(
            @RequestParam(required = false) UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getEmployeeReport(companyId)));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project progress report")
    public ResponseEntity<ApiResponse<ProjectReportResponse>> getProjectReport(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getProjectReport(projectId)));
    }
}
