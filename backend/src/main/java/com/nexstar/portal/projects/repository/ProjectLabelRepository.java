package com.nexstar.portal.projects.repository;

import com.nexstar.portal.projects.entity.ProjectLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectLabelRepository extends JpaRepository<ProjectLabel, UUID> {

    List<ProjectLabel> findByProjectIdAndDeletedFalse(UUID projectId);
}
