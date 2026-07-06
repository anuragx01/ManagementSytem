package com.nexstar.portal.projects.repository;

import com.nexstar.portal.projects.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    List<Milestone> findByProjectIdAndDeletedFalse(UUID projectId);

    Optional<Milestone> findByIdAndDeletedFalse(UUID id);
}
