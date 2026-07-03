package com.nexstar.portal.dailyreport.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.dailyreport.dto.DailyWorkReportDto;
import com.nexstar.portal.dailyreport.service.DailyWorkReportService;
import com.nexstar.portal.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/daily-reports")
@RequiredArgsConstructor
public class DailyWorkReportController {
    private final DailyWorkReportService dailyWorkReportService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DailyWorkReportDto>>> myReports(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(dailyWorkReportService.myReports(currentUser)));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('TEAM_LEAD')")
    public ResponseEntity<ApiResponse<List<DailyWorkReportDto>>> allReports() {
        return ResponseEntity.ok(ApiResponse.success(dailyWorkReportService.allReports()));
    }

    @PostMapping("/my")
    public ResponseEntity<ApiResponse<DailyWorkReportDto>> saveMyReport(
            @Valid @RequestBody DailyWorkReportDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Daily report saved", dailyWorkReportService.saveMyReport(dto, currentUser)));
    }
}
