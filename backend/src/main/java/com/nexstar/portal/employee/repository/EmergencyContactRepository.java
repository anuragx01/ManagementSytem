package com.nexstar.portal.employee.repository;

import com.nexstar.portal.employee.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {
    List<EmergencyContact> findByEmployeeIdAndDeletedFalse(UUID employeeId);
    Optional<EmergencyContact> findByIdAndDeletedFalse(UUID id);
}
