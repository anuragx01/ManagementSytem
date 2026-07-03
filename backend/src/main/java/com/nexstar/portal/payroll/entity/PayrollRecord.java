package com.nexstar.portal.payroll.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payroll_records", indexes = {
        @Index(name = "idx_payroll_employee_id", columnList = "employee_id"),
        @Index(name = "idx_payroll_period", columnList = "payroll_period"),
        @Index(name = "idx_payroll_status", columnList = "payment_status")
})
public class PayrollRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payroll_period", nullable = false, length = 20)
    private String payrollPeriod;

    @Column(name = "basic_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal allowances;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "processed_date")
    private LocalDate processedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum PaymentStatus {
        PENDING, PROCESSING, PAID, FAILED, HOLD
    }
}
