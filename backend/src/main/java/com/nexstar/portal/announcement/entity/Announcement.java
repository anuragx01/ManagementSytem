package com.nexstar.portal.announcement.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "announcements")
public class Announcement extends BaseEntity {
    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "audience_role", length = 40)
    private String audienceRole;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
