package com.nexstar.portal.dailyreport.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.dailyreport.dto.DailyWorkReportDto;
import com.nexstar.portal.dailyreport.entity.DailyWorkReport;
import com.nexstar.portal.dailyreport.repository.DailyWorkReportRepository;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyWorkReportService {
    private final DailyWorkReportRepository dailyWorkReportRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DailyWorkReportDto> myReports(UserPrincipal currentUser) {
        Employee employee = employeeByUser(currentUser);
        return dailyWorkReportRepository.findByEmployee_IdAndDeletedFalseOrderByReportDateDesc(employee.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DailyWorkReportDto> allReports() {
        return dailyWorkReportRepository.findByDeletedFalseOrderByReportDateDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public DailyWorkReportDto saveMyReport(DailyWorkReportDto dto, UserPrincipal currentUser) {
        Employee employee = employeeByUser(currentUser);
        LocalDate reportDate = dto.getReportDate() != null ? dto.getReportDate() : LocalDate.now();
        DailyWorkReport report = dailyWorkReportRepository
                .findByEmployee_IdAndReportDateAndDeletedFalse(employee.getId(), reportDate)
                .orElseGet(() -> DailyWorkReport.builder()
                        .employee(employee)
                        .reportDate(reportDate)
                        .build());
        if (report.getStatus() == DailyWorkReport.ReportStatus.SUBMITTED) {
            throw new BusinessException("Daily report is already submitted for this date.");
        }
        report.setCompletedWork(dto.getCompletedWork());
        report.setPendingWork(dto.getPendingWork());
        report.setTomorrowPlan(dto.getTomorrowPlan());
        report.setBlockers(dto.getBlockers());
        report.setStatus(dto.getStatus() != null ? dto.getStatus() : DailyWorkReport.ReportStatus.SUBMITTED);
        report.setSubmittedAt(LocalDateTime.now());
        return toDto(dailyWorkReportRepository.save(report));
    }

    private Employee employeeByUser(UserPrincipal currentUser) {
        return employeeRepository.findByUserIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new BusinessException("Employee profile not found.", HttpStatus.NOT_FOUND));
    }

    private DailyWorkReportDto toDto(DailyWorkReport report) {
        Employee employee = report.getEmployee();
        String employeeName = employee != null && employee.getUser() != null
                ? employee.getUser().getFirstName() + " " + employee.getUser().getLastName()
                : null;
        return DailyWorkReportDto.builder()
                .id(report.getId())
                .employeeId(employee != null ? employee.getId() : null)
                .employeeName(employeeName)
                .reportDate(report.getReportDate())
                .completedWork(report.getCompletedWork())
                .pendingWork(report.getPendingWork())
                .tomorrowPlan(report.getTomorrowPlan())
                .blockers(report.getBlockers())
                .status(report.getStatus())
                .submittedAt(report.getSubmittedAt())
                .build();
    }
}
