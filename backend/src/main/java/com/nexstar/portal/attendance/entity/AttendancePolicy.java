package com.nexstar.portal.attendance.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.organization.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "attendance_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePolicy extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "office_start_time", nullable = false)
    private LocalTime officeStartTime;

    @Column(name = "office_end_time", nullable = false)
    private LocalTime officeEndTime;

    /** Grace period in minutes before marking late */
    @Column(name = "grace_time_minutes", nullable = false)
    @Builder.Default
    private int graceTimeMinutes = 15;

    /** Minutes worked to qualify as half day */
    @Column(name = "half_day_hours", nullable = false)
    @Builder.Default
    private int halfDayHours = 4;

    /** Full working hours expected per day */
    @Column(name = "full_day_hours", nullable = false)
    @Builder.Default
    private int fullDayHours = 8;

    /** Minutes after which overtime is counted */
    @Column(name = "overtime_threshold_minutes", nullable = false)
    @Builder.Default
    private int overtimeThresholdMinutes = 30;

    /** Working days bitmask bit0=Mon...bit6=Sun (62 = Mon–Fri) */
    @Column(name = "working_days_mask", nullable = false)
    @Builder.Default
    private int workingDaysMask = 62;

    @Column(name = "allow_work_from_home", nullable = false)
    @Builder.Default
    private boolean allowWorkFromHome = true;

    @Column(name = "require_location", nullable = false)
    @Builder.Default
    private boolean requireLocation = false;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
