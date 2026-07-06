package com.nexstar.portal.leave.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "leave_type_id", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(name = "allocated_days", nullable = false)
    @Builder.Default
    private double allocatedDays = 0;

    @Column(name = "carried_forward_days", nullable = false)
    @Builder.Default
    private double carriedForwardDays = 0;

    @Column(name = "used_days", nullable = false)
    @Builder.Default
    private double usedDays = 0;

    @Column(name = "pending_days", nullable = false)
    @Builder.Default
    private double pendingDays = 0;   // In approval

    public double getAvailableDays() {
        return allocatedDays + carriedForwardDays - usedDays - pendingDays;
    }
}
