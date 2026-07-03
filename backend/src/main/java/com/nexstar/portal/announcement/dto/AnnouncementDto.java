package com.nexstar.portal.announcement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private UUID id;
    @NotBlank
    private String title;
    @NotBlank
    private String message;
    private String audienceRole;
    private boolean active;
    private LocalDateTime publishedAt;
}
