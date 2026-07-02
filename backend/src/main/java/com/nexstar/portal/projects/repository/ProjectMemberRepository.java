package com.nexstar.portal.projects.repository;

import com.nexstar.portal.projects.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    List<ProjectMember> findByProjectIdAndDeletedFalse(UUID projectId);

    Optional<ProjectMember> findByProjectIdAndEmployeeIdAndDeletedFalse(UUID projectId, UUID employeeId);

    boolean existsByProjectIdAndEmployeeIdAndDeletedFalse(UUID projectId, UUID employeeId);

    @Modifying
    @Query("UPDATE ProjectMember pm SET pm.deleted = true WHERE pm.project.id = :projectId AND pm.employee.id = :employeeId")
    void deleteByProjectIdAndEmployeeId(UUID projectId, UUID employeeId);

    long countByProjectIdAndDeletedFalse(UUID projectId);
}
