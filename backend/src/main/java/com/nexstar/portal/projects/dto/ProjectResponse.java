package com.nexstar.portal.projects.dto;

import com.nexstar.portal.projects.entity.Project;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private UUID id;
    private String name;
    private String key;
    private String description;
    private UUID companyId;
    private String companyName;
    private UUID ownerId;
    private String ownerName;
    private Project.ProjectStatus status;
    private Project.ProjectType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String logoUrl;
    private boolean isPrivate;
    private boolean archived;
    private long memberCount;
    private LocalDateTime createdAt;
}
