package com.nexstar.portal.projects.dto;

import com.nexstar.portal.projects.entity.ProjectMember;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberDto {

    private UUID id;
    private UUID projectId;
    private UUID employeeId;
    private String employeeName;
    private ProjectMember.MemberRole role;
    private LocalDateTime joinedAt;
}
