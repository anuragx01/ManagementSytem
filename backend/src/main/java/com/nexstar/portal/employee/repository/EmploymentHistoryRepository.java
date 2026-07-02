package com.nexstar.portal.employee.repository;

import com.nexstar.portal.employee.entity.EmploymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, UUID> {
    List<EmploymentHistory> findByEmployeeIdAndDeletedFalseOrderByStartDateDesc(UUID employeeId);
    Optional<EmploymentHistory> findByIdAndDeletedFalse(UUID id);
}
