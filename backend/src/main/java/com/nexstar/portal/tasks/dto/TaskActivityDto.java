package com.nexstar.portal.tasks.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskActivityDto {

    private UUID id;
    private UUID taskId;
    private UUID actorId;
    private String actorName;
    private String action;
    private String from;
    private String to;
    private String detail;
    private LocalDateTime createdAt;
}
