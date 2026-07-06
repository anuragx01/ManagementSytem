package com.nexstar.portal.organization.repository;

import com.nexstar.portal.organization.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByDepartmentIdAndDeletedFalse(UUID departmentId);
    List<Team> findByDepartmentCompanyIdAndDeletedFalse(UUID companyId);
    Optional<Team> findByIdAndDeletedFalse(UUID id);
    boolean existsByNameAndDepartmentIdAndDeletedFalse(String name, UUID departmentId);
}
