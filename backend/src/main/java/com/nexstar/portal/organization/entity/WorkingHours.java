package com.nexstar.portal.organization.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "working_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHours extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    // Working days bitmask: bit 0=Mon, 1=Tue, ..., 6=Sun
    @Column(name = "working_days_mask", nullable = false)
    @Builder.Default
    private int workingDaysMask = 62; // Mon-Fri by default (0b0111110)

    @Column(name = "grace_time_minutes", nullable = false)
    @Builder.Default
    private int graceTimeMinutes = 10;

    @Column(name = "is_flexible", nullable = false)
    @Builder.Default
    private boolean flexible = false;

    @Column(name = "flexible_hours_per_day")
    private Integer flexibleHoursPerDay;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
