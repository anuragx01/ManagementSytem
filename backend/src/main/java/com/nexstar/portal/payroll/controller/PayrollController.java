package com.nexstar.portal.payroll.controller;

import com.nexstar.portal.common.response.ApiResponse;
import com.nexstar.portal.payroll.dto.PayrollRecordDto;
import com.nexstar.portal.payroll.service.PayrollService;
import com.nexstar.portal.security.UserPrincipal;
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
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> listPayroll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) String payrollPeriod,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.listPayroll(employeeId, payrollPeriod, currentUser)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PayrollRecordDto>>> myPayroll(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.myPayroll(currentUser)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<PayrollRecordDto>> createPayroll(@Valid @RequestBody PayrollRecordDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Payroll record created", payrollService.createPayroll(dto)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('HR')")
    public ResponseEntity<ApiResponse<PayrollRecordDto>> updatePayroll(
            @PathVariable UUID id,
            @RequestBody PayrollRecordDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Payroll record updated", payrollService.updatePayroll(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePayroll(@PathVariable UUID id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.noContent("Payroll record deleted"));
    }
}
