package com.nexstar.portal.leave.repository;

import com.nexstar.portal.leave.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    Optional<LeaveRequest> findByIdAndDeletedFalse(UUID id);

    Page<LeaveRequest> findByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatusAndDeletedFalse(LeaveRequest.LeaveStatus status, Pageable pageable);

    /** Pending requests for a manager's direct reports */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.reportingManager.id = :managerId
            AND lr.status = 'PENDING'
            AND lr.deleted = false
            """)
    List<LeaveRequest> findPendingForManager(UUID managerId);

    /** All requests pending HR approval */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.status = 'MANAGER_APPROVED'
            AND lr.deleted = false
            """)
    List<LeaveRequest> findPendingForHr();

    /** Leave requests overlapping a date range for an employee */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
            AND lr.deleted = false
            AND lr.status NOT IN ('REJECTED','CANCELLED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findOverlapping(UUID employeeId, LocalDate startDate, LocalDate endDate);

    /** Team leaves for calendar view */
    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.team.id = :teamId
            AND lr.deleted = false
            AND lr.status IN ('APPROVED','AUTO_APPROVED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findApprovedByTeamAndDateRange(
            UUID teamId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.deleted = false
            AND lr.status IN ('APPROVED','AUTO_APPROVED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findApprovedByDateRange(LocalDate startDate, LocalDate endDate);
}
