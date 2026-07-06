package com.nexstar.portal.attendance.repository;

import com.nexstar.portal.attendance.entity.AttendanceBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceBreakRepository extends JpaRepository<AttendanceBreak, UUID> {

    List<AttendanceBreak> findByAttendanceRecordIdOrderByBreakStartAsc(UUID recordId);

    /** Find an open break (started but not ended yet) */
    @Query("""
            SELECT b FROM AttendanceBreak b
            WHERE b.attendanceRecord.id = :recordId
            AND b.breakEnd IS NULL
            AND b.deleted = false
            """)
    Optional<AttendanceBreak> findOpenBreak(UUID recordId);
}
