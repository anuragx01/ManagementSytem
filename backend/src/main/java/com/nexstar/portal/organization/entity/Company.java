package com.nexstar.portal.organization.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String logoUrl;

    @Column(length = 100)
    private String industry;

    @Column(length = 255)
    private String website;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 50)
    private String timezone;

    @Column(length = 10)
    private String currency;

    @Column(length = 10)
    private String dateFormat;

    @Column(columnDefinition = "TEXT")
    private String description;
}
