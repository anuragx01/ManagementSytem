package com.nexstar.portal.employee.repository;

import com.nexstar.portal.employee.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, UUID> {
    List<EmployeeSkill> findByEmployeeIdAndDeletedFalse(UUID employeeId);
    Optional<EmployeeSkill> findByIdAndDeletedFalse(UUID id);
}
