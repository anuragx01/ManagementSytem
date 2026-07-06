package com.nexstar.portal.employee.repository;

import com.nexstar.portal.employee.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {
    List<EmployeeDocument> findByEmployeeIdAndDeletedFalse(UUID employeeId);
    Optional<EmployeeDocument> findByIdAndDeletedFalse(UUID id);
}
