package com.nexstar.portal.leave.repository;

import com.nexstar.portal.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, UUID> {
    List<LeaveType> findByCompanyIdAndDeletedFalse(UUID companyId);
    List<LeaveType> findByCompanyIdAndActiveAndDeletedFalse(UUID companyId, boolean active);
    Optional<LeaveType> findByIdAndDeletedFalse(UUID id);
    boolean existsByNameAndCompanyIdAndDeletedFalse(String name, UUID companyId);
}
