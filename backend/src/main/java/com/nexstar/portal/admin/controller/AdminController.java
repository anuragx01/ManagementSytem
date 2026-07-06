package com.nexstar.portal.admin.controller;

import com.nexstar.portal.admin.entity.AuditLog;
import com.nexstar.portal.admin.service.AuditService;
import com.nexstar.portal.authentication.repository.UserRepository;
import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.projects.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Admin and audit log endpoints")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AuditService auditService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs with optional filters")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getLogs(page, size, action, entityType)));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<PageResponse<AuditLog>>> getUserLogs(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getUserLogs(userId, page, size)));
    }

    @GetMapping("/system/summary")
    @Operation(summary = "Get system summary counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("users", userRepository.count());
        summary.put("employees", employeeRepository.count());
        summary.put("projects", projectRepository.count());
        summary.put("activeAttendance", 0L);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
