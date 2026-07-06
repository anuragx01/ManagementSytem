package com.nexstar.portal.timetracking.repository;

import com.nexstar.portal.timetracking.entity.Timesheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    Optional<Timesheet> findByEmployeeIdAndWeekStartDateAndDeletedFalse(UUID employeeId, LocalDate weekStartDate);

    Page<Timesheet> findByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    Page<Timesheet> findByStatusAndDeletedFalse(Timesheet.TimesheetStatus status, Pageable pageable);

    List<Timesheet> findByApproverIdAndStatusAndDeletedFalse(UUID approverId, Timesheet.TimesheetStatus status);

    Optional<Timesheet> findByIdAndDeletedFalse(UUID id);
}
