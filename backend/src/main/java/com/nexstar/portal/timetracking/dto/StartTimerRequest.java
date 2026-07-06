package com.nexstar.portal.timetracking.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class StartTimerRequest {
    private UUID projectId;
    private UUID taskId;
    private String description;
    private boolean billable = true;
}
