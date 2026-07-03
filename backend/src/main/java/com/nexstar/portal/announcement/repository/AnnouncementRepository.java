package com.nexstar.portal.announcement.repository;

import com.nexstar.portal.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    List<Announcement> findByDeletedFalseOrderByPublishedAtDescCreatedAtDesc();
    List<Announcement> findByActiveTrueAndDeletedFalseOrderByPublishedAtDescCreatedAtDesc();
}
