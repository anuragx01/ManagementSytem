package com.nexstar.portal.timetracking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class WeeklySummaryResponse {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Map<DayOfWeek, Integer> dailySummary;
    private int totalMinutes;
    private int billableMinutes;
    private List<ProjectBreakdown> projectBreakdown;

    @Data
    @Builder
    public static class ProjectBreakdown {
        private UUID projectId;
        private String projectName;
        private int minutes;
        private int billableMinutes;
    }
}
