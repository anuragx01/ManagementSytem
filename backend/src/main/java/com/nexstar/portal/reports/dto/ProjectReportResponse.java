package com.nexstar.portal.reports.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ProjectReportResponse {
    private UUID projectId;
    private String name;
    private Map<String, Long> tasksByStatus;
    private long totalTasks;
    private long overdueTasks;
    private long totalLoggedMinutes;
    private long totalEstimatedMinutes;
}
