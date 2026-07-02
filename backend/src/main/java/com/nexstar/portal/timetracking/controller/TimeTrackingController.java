package com.nexstar.portal.timetracking.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.timetracking.dto.*;
import com.nexstar.portal.timetracking.service.TimeTrackingService;
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
@RequestMapping("/time")
@RequiredArgsConstructor
@Tag(name = "Time Tracking", description = "Time tracking and timesheet management endpoints")
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    // ── Timer Endpoints ────────────────────────────────────────────────────────

    @PostMapping("/timer/start")
    @Operation(summary = "Start a timer")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> startTimer(
            @RequestBody(required = false) StartTimerRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (request == null) request = new StartTimerRequest();
        return ResponseEntity.ok(ApiResponse.success("Timer started",
                timeTrackingService.startTimer(request, currentUser)));
    }

    @PostMapping("/timer/pause")
    @Operation(summary = "Pause the running timer")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> pauseTimer(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Timer paused",
                timeTrackingService.pauseTimer(currentUser)));
    }

    @PostMapping("/timer/resume")
    @Operation(summary = "Resume a paused timer")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> resumeTimer(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Timer resumed",
                timeTrackingService.resumeTimer(currentUser)));
    }

    @PostMapping("/timer/stop")
    @Operation(summary = "Stop the active timer")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> stopTimer(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Timer stopped",
                timeTrackingService.stopTimer(currentUser)));
    }

    @GetMapping("/timer/status")
    @Operation(summary = "Get current timer status")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> getTimerStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(timeTrackingService.getTimerStatus(currentUser)));
    }

    // ── Time Log Endpoints ─────────────────────────────────────────────────────

    @PostMapping("/log")
    @Operation(summary = "Log time manually")
    public ResponseEntity<ApiResponse<TimeEntryResponse>> logTimeManually(
            @Valid @RequestBody TimeEntryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(201).body(ApiResponse.created("Time entry logged",
                timeTrackingService.logTimeManually(request, currentUser)));
    }

    @GetMapping("/entries")
    @Operation(summary = "Get my time entries for a date range")
    public ResponseEntity<ApiResponse<List<TimeEntryResponse>>> getMyTimeEntries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                timeTrackingService.getMyTimeEntries(from, to, currentUser)));
    }

    @GetMapping("/weekly")
    @Operation(summary = "Get weekly summary")
    public ResponseEntity<ApiResponse<WeeklySummaryResponse>> getWeeklySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                timeTrackingService.getWeeklySummary(weekStart, currentUser)));
    }

    // ── Timesheet Endpoints ────────────────────────────────────────────────────

    @PostMapping("/timesheet/submit")
    @Operation(summary = "Submit timesheet for a week")
    public ResponseEntity<ApiResponse<TimesheetResponse>> submitTimesheet(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Timesheet submitted",
                timeTrackingService.submitTimesheet(weekStart, currentUser)));
    }

    @PostMapping("/timesheet/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Approve or reject a timesheet")
    public ResponseEntity<ApiResponse<TimesheetResponse>> approveTimesheet(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "true") boolean approve,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                approve ? "Timesheet approved" : "Timesheet rejected",
                timeTrackingService.approveTimesheet(id, remarks, approve, currentUser)));
    }

    @GetMapping("/timesheet/pending")
    @PreAuthorize("hasRole('MANAGER') or hasRole('HR') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get pending timesheets for approval")
    public ResponseEntity<ApiResponse<List<TimesheetResponse>>> getPendingTimesheets(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                timeTrackingService.getPendingTimesheets(currentUser)));
    }

    @GetMapping("/timesheet/my")
    @Operation(summary = "Get my timesheets")
    public ResponseEntity<ApiResponse<PageResponse<TimesheetResponse>>> getMyTimesheets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                timeTrackingService.getMyTimesheets(page, size, currentUser)));
    }
}
