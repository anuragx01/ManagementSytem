package com.nexstar.portal.announcement.service;

import com.nexstar.portal.announcement.dto.AnnouncementDto;
import com.nexstar.portal.announcement.entity.Announcement;
import com.nexstar.portal.announcement.repository.AnnouncementRepository;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementDto> list(boolean includeInactive) {
        return (includeInactive
                ? announcementRepository.findByDeletedFalseOrderByPublishedAtDescCreatedAtDesc()
                : announcementRepository.findByActiveTrueAndDeletedFalseOrderByPublishedAtDescCreatedAtDesc())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public AnnouncementDto create(AnnouncementDto dto) {
        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .message(dto.getMessage())
                .audienceRole(dto.getAudienceRole())
                .active(true)
                .publishedAt(LocalDateTime.now())
                .build();
        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementDto update(UUID id, AnnouncementDto dto) {
        Announcement announcement = get(id);
        if (dto.getTitle() != null) announcement.setTitle(dto.getTitle());
        if (dto.getMessage() != null) announcement.setMessage(dto.getMessage());
        if (dto.getAudienceRole() != null) announcement.setAudienceRole(dto.getAudienceRole());
        announcement.setActive(dto.isActive());
        return toDto(announcementRepository.save(announcement));
    }

    @Transactional
    public void delete(UUID id) {
        Announcement announcement = get(id);
        announcement.setDeleted(true);
        announcementRepository.save(announcement);
    }

    private Announcement get(UUID id) {
        return announcementRepository.findById(id)
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
    }

    private AnnouncementDto toDto(Announcement announcement) {
        return AnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .message(announcement.getMessage())
                .audienceRole(announcement.getAudienceRole())
                .active(announcement.isActive())
                .publishedAt(announcement.getPublishedAt())
                .build();
    }
}
