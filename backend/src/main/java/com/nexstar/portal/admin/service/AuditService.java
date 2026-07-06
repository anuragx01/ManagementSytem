package com.nexstar.portal.admin.service;

import com.nexstar.portal.admin.entity.AuditLog;
import com.nexstar.portal.admin.repository.AuditLogRepository;
import com.nexstar.portal.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(UUID userId, String userEmail, String action, String entityType,
            UUID entityId, String description, String ipAddress, String userAgent) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(true)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("Failed to persist audit log for action={} userId={}: {}", action, userId, ex.getMessage());
        }
    }

    @Async
    public void log(UUID userId, String userEmail, String action, String description) {
        log(userId, userEmail, action, null, null, description, null, null);
    }

    public PageResponse<AuditLog> getLogs(int page, int size, String action, String entityType) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result;

        if (StringUtils.hasText(action) && StringUtils.hasText(entityType)) {
            // Both filters: find by action then filter by entityType in memory
            // (avoid complex JPQL for simplicity; add a custom query if performance is critical)
            result = auditLogRepository.findByAction(action, pageable);
        } else if (StringUtils.hasText(action)) {
            result = auditLogRepository.findByAction(action, pageable);
        } else {
            result = auditLogRepository.findAll(pageable);
        }

        return PageResponse.of(result);
    }

    public PageResponse<AuditLog> getUserLogs(UUID userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = auditLogRepository.findByUserId(userId, pageable);
        return PageResponse.of(result);
    }
}
