package com.nexstar.portal.payroll.dto;

import com.nexstar.portal.payroll.entity.PayrollRecord;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecordDto {
    private UUID id;
    @NotNull
    private UUID employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    @NotNull
    private String payrollPeriod;
    @NotNull
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private PayrollRecord.PaymentStatus paymentStatus;
    private LocalDate processedDate;
    private String notes;
    private LocalDateTime createdAt;
}
