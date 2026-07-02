package com.nexstar.portal.tasks.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCommentDto {

    private UUID id;
    private UUID taskId;
    private UUID authorId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
