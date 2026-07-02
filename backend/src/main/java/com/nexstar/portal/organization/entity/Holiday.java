package com.nexstar.portal.organization.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "holidays")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HolidayType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private boolean optional = false;

    public enum HolidayType {
        NATIONAL, COMPANY, CUSTOM
    }
}
