package com.nexstar.portal.organization.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 50)
    private String timezone;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_headquarters", nullable = false)
    @Builder.Default
    private boolean headquarters = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
