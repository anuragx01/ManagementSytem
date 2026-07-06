package com.nexstar.portal.attendance.repository;

import com.nexstar.portal.attendance.entity.AttendancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendancePolicyRepository extends JpaRepository<AttendancePolicy, UUID> {
    List<AttendancePolicy> findByCompanyIdAndDeletedFalse(UUID companyId);
    Optional<AttendancePolicy> findByCompanyIdAndIsDefaultTrueAndDeletedFalse(UUID companyId);
    Optional<AttendancePolicy> findByIdAndDeletedFalse(UUID id);
}
