package com.nexstar.portal.dailyreport.repository;

import com.nexstar.portal.dailyreport.entity.DailyWorkReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyWorkReportRepository extends JpaRepository<DailyWorkReport, UUID> {
    Optional<DailyWorkReport> findByEmployee_IdAndReportDateAndDeletedFalse(UUID employeeId, LocalDate reportDate);
    List<DailyWorkReport> findByEmployee_IdAndDeletedFalseOrderByReportDateDesc(UUID employeeId);
    List<DailyWorkReport> findByDeletedFalseOrderByReportDateDesc();
}
