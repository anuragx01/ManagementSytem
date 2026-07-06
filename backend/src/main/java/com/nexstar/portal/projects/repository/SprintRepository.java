package com.nexstar.portal.projects.repository;

import com.nexstar.portal.projects.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    List<Sprint> findByProjectIdAndDeletedFalse(UUID projectId);

    List<Sprint> findByProjectIdAndStatusAndDeletedFalse(UUID projectId, Sprint.SprintStatus status);

    Optional<Sprint> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = 'ACTIVE' AND s.deleted = false")
    Optional<Sprint> findActiveSprintByProjectId(UUID projectId);
}
