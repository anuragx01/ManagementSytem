package com.nexstar.portal.organization.repository;

import com.nexstar.portal.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByCompanyIdAndDeletedFalse(UUID companyId);
    List<Department> findByCompanyIdAndActiveAndDeletedFalse(UUID companyId, boolean active);
    Optional<Department> findByIdAndDeletedFalse(UUID id);
    boolean existsByNameAndCompanyIdAndDeletedFalse(String name, UUID companyId);
}
