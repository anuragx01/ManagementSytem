package com.nexstar.portal.leave.repository;

import com.nexstar.portal.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    List<LeaveBalance> findByEmployeeIdAndYearAndDeletedFalse(UUID employeeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
            UUID employeeId, UUID leaveTypeId, int year);

    @Query("""
            SELECT lb FROM LeaveBalance lb
            JOIN FETCH lb.leaveType
            WHERE lb.employee.id = :employeeId AND lb.year = :year AND lb.deleted = false
            """)
    List<LeaveBalance> findByEmployeeAndYearWithType(UUID employeeId, int year);
}
