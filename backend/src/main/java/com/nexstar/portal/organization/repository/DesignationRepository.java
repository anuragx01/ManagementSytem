package com.nexstar.portal.organization.repository;

import com.nexstar.portal.organization.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {
    List<Designation> findByCompanyIdAndDeletedFalse(UUID companyId);
    List<Designation> findByCompanyIdAndActiveAndDeletedFalse(UUID companyId, boolean active);
    Optional<Designation> findByIdAndDeletedFalse(UUID id);
    boolean existsByNameAndCompanyIdAndDeletedFalse(String name, UUID companyId);
}
