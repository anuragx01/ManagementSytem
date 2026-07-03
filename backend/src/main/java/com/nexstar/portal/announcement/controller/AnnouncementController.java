package com.nexstar.portal.announcement.controller;

import com.nexstar.portal.announcement.dto.AnnouncementDto;
import com.nexstar.portal.announcement.service.AnnouncementService;
import com.nexstar.portal.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnouncementDto>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(ApiResponse.success(announcementService.list(includeInactive)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> create(@Valid @RequestBody AnnouncementDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Announcement created", announcementService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementDto>> update(
            @PathVariable UUID id,
            @RequestBody AnnouncementDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Announcement updated", announcementService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        announcementService.delete(id);
        return ResponseEntity.ok(ApiResponse.noContent("Announcement deleted"));
    }
}
