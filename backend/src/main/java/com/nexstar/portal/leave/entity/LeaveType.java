package com.nexstar.portal.leave.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.organization.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_days_per_year", nullable = false)
    private int maxDaysPerYear;

    /** Maximum consecutive days allowed per request */
    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private boolean paid = true;

    /** Carry-forward allowed? */
    @Column(name = "carry_forward_allowed", nullable = false)
    @Builder.Default
    private boolean carryForwardAllowed = false;

    @Column(name = "max_carry_forward_days")
    private Integer maxCarryForwardDays;

    /** Can be taken in half-day units? */
    @Column(name = "half_day_allowed", nullable = false)
    @Builder.Default
    private boolean halfDayAllowed = true;

    /** Document required for this leave type? */
    @Column(name = "document_required", nullable = false)
    @Builder.Default
    private boolean documentRequired = false;

    /** Minimum advance notice in days */
    @Column(name = "min_advance_days")
    private Integer minAdvanceDays;

    /** Colour code for calendar display */
    @Column(length = 7)
    private String color;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
