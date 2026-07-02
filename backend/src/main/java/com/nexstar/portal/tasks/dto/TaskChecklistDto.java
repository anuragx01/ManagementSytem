package com.nexstar.portal.tasks.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskChecklistDto {

    private UUID id;
    private UUID taskId;
    private String title;
    private boolean completed;
    private int position;
}
