package com.nexstar.portal.timetracking.repository;

import com.nexstar.portal.timetracking.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByEmployeeIdAndDateBetweenAndDeletedFalse(UUID employeeId, LocalDate from, LocalDate to);

    Optional<TimeEntry> findByEmployeeIdAndTimerStatusAndDeletedFalse(UUID employeeId, TimeEntry.TimerStatus timerStatus);

    List<TimeEntry> findByTimesheetIdAndDeletedFalse(UUID timesheetId);

    List<TimeEntry> findByEmployeeIdAndDateAndDeletedFalse(UUID employeeId, LocalDate date);

    Optional<TimeEntry> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT SUM(t.durationMinutes) FROM TimeEntry t WHERE t.employee.id = :eid AND t.date BETWEEN :from AND :to AND t.deleted = false")
    Optional<Long> sumMinutesByEmployeeAndDateRange(@Param("eid") UUID eid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT SUM(t.durationMinutes) FROM TimeEntry t WHERE t.project.id = :pid AND t.deleted = false")
    Optional<Long> sumMinutesByProject(@Param("pid") UUID pid);

    @Query("SELECT t FROM TimeEntry t WHERE t.employee.id = :employeeId AND t.timerStatus IN ('RUNNING', 'PAUSED') AND t.deleted = false")
    Optional<TimeEntry> findActiveTimerByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT t FROM TimeEntry t WHERE t.employee.id = :employeeId AND t.timerStatus = 'PAUSED' AND t.deleted = false")
    Optional<TimeEntry> findPausedTimerByEmployeeId(@Param("employeeId") UUID employeeId);
}
