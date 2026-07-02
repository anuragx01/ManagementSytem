package com.nexstar.portal.projects.repository;

import com.nexstar.portal.projects.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByCompanyIdAndDeletedFalse(UUID companyId, Pageable pageable);

    List<Project> findByCompanyIdAndDeletedFalse(UUID companyId);

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    Optional<Project> findByKeyAndDeletedFalse(String key);

    boolean existsByKeyAndCompanyIdAndDeletedFalse(String key, UUID companyId);
}
