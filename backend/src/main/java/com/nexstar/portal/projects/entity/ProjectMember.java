package com.nexstar.portal.projects.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import com.nexstar.portal.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_members", indexes = {
        @Index(name = "idx_project_members_project_id", columnList = "project_id"),
        @Index(name = "idx_project_members_employee_id", columnList = "employee_id")
},
        uniqueConstraints = @UniqueConstraint(
                name = "uq_project_member",
                columnNames = {"project_id", "employee_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.DEVELOPER;

    @Column(name = "joined_at")
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum MemberRole {
        OWNER, MANAGER, DEVELOPER, VIEWER
    }
}
