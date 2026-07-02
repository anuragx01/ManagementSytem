package com.nexstar.portal.tasks.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanResponse {

    private Map<String, List<TaskResponse>> columns;
}
