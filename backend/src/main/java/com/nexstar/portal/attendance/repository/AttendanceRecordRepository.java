package com.nexstar.portal.attendance.repository;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
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
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByEmployeeIdAndDate(UUID employeeId, LocalDate date);

    List<AttendanceRecord> findByEmployeeIdAndDateBetweenOrderByDateAsc(
            UUID employeeId, LocalDate from, LocalDate to);

    List<AttendanceRecord> findByDateAndDeletedFalse(LocalDate date);

    Page<AttendanceRecord> findAllByDeletedFalse(Pageable pageable);

    Page<AttendanceRecord> findByEmployeeIdAndDeletedFalse(UUID employeeId, Pageable pageable);

    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE a.date = :date AND a.deleted = false
            AND a.employee.department.id = :departmentId
            """)
    List<AttendanceRecord> findByDateAndDepartmentId(LocalDate date, UUID departmentId);

    @Query("""
            SELECT COUNT(a) FROM AttendanceRecord a
            WHERE a.date = :date AND a.status = :status AND a.deleted = false
            """)
    long countByDateAndStatus(LocalDate date, AttendanceRecord.AttendanceStatus status);

    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE a.employee.id = :employeeId
            AND a.date BETWEEN :from AND :to
            AND a.deleted = false
            ORDER BY a.date ASC
            """)
    List<AttendanceRecord> findByEmployeeAndDateRange(UUID employeeId, LocalDate from, LocalDate to);

    /** Check if employee has an open (clocked-in, not clocked-out) record today */
    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE a.employee.id = :employeeId
            AND a.date = :date
            AND a.clockIn IS NOT NULL
            AND a.clockOut IS NULL
            AND a.deleted = false
            """)
    Optional<AttendanceRecord> findOpenRecord(UUID employeeId, LocalDate date);
}
