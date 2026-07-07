package com.nexstar.portal.reports.service;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import com.nexstar.portal.attendance.repository.AttendanceRecordRepository;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.leave.entity.LeaveRequest;
import com.nexstar.portal.leave.repository.LeaveRequestRepository;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.reports.dto.*;
import com.nexstar.portal.timetracking.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;

    // ── Attendance Report ──────────────────────────────────────────────────────

    public AttendanceReportResponse getAttendanceReport(
            UUID companyId,
            LocalDate from,
            LocalDate to) {

        List<Employee> allEmployees = employeeRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());

        Map<UUID, List<AttendanceRecord>> recordsByEmployee =
                new HashMap<>();

        for (Employee emp : allEmployees) {
            List<AttendanceRecord> records =
                    attendanceRecordRepository.findByEmployeeAndDateRange(
                            emp.getId(),
                            from,
                            to
                    );

            if (!records.isEmpty()) {
                recordsByEmployee.put(emp.getId(), records);
            }
        }

        long totalRecords = recordsByEmployee.values()
                .stream()
                .mapToLong(List::size)
                .sum();

        long presentCount = recordsByEmployee.values()
                .stream()
                .flatMap(List::stream)
                .filter(r ->
                        r.getStatus()
                                == AttendanceRecord.AttendanceStatus.PRESENT
                                || r.getStatus()
                                == AttendanceRecord.AttendanceStatus.LATE
                                || r.getStatus()
                                == AttendanceRecord.AttendanceStatus.WORK_FROM_HOME
                )
                .count();

        long absentCount = recordsByEmployee.values()
                .stream()
                .flatMap(List::stream)
                .filter(r ->
                        r.getStatus()
                                == AttendanceRecord.AttendanceStatus.ABSENT
                )
                .count();

        long lateCount = recordsByEmployee.values()
                .stream()
                .flatMap(List::stream)
                .filter(r ->
                        r.getStatus()
                                == AttendanceRecord.AttendanceStatus.LATE
                )
                .count();

        List<AttendanceReportResponse.EmployeeAttendanceSummary>
                employeeSummaries = new ArrayList<>();

        for (Employee emp : allEmployees) {

            List<AttendanceRecord> records =
                    recordsByEmployee.getOrDefault(
                            emp.getId(),
                            Collections.emptyList()
                    );

            if (records.isEmpty()) {
                continue;
            }

            String name = "";

            if (emp.getUser() != null) {
                name = emp.getUser().getFirstName()
                        + " "
                        + emp.getUser().getLastName();
            }

            int presentDays = (int) records.stream()
                    .filter(r ->
                            r.getStatus()
                                    == AttendanceRecord.AttendanceStatus.PRESENT
                                    || r.getStatus()
                                    == AttendanceRecord.AttendanceStatus.LATE
                                    || r.getStatus()
                                    == AttendanceRecord.AttendanceStatus.WORK_FROM_HOME
                    )
                    .count();

            int absentDays = (int) records.stream()
                    .filter(r ->
                            r.getStatus()
                                    == AttendanceRecord.AttendanceStatus.ABSENT
                    )
                    .count();

            int lateDays = (int) records.stream()
                    .filter(r ->
                            r.getStatus()
                                    == AttendanceRecord.AttendanceStatus.LATE
                    )
                    .count();

            int workedMinutes = records.stream()
                    .filter(r -> r.getWorkedMinutes() != null)
                    .mapToInt(AttendanceRecord::getWorkedMinutes)
                    .sum();

            employeeSummaries.add(
                    AttendanceReportResponse.EmployeeAttendanceSummary
                            .builder()
                            .employeeId(emp.getId())
                            .name(name)
                            .presentDays(presentDays)
                            .absentDays(absentDays)
                            .lateDays(lateDays)
                            .workedMinutes(workedMinutes)
                            .build()
            );
        }

        return AttendanceReportResponse.builder()
                .from(from)
                .to(to)
                .totalRecords(totalRecords)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .employees(employeeSummaries)
                .build();
    }

    // ── Leave Report ───────────────────────────────────────────────────────────

    public LeaveReportResponse getLeaveReport(
            UUID companyId,
            LocalDate from,
            LocalDate to) {

        List<LeaveRequest> allRequests =
                leaveRequestRepository.findAll()
                        .stream()
                        .filter(r ->
                                !r.isDeleted()
                                        && !r.getStartDate().isAfter(to)
                                        && !r.getEndDate().isBefore(from)
                        )
                        .collect(Collectors.toList());

        Map<String, List<LeaveRequest>> byType =
                allRequests.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getLeaveType() != null
                                        ? r.getLeaveType().getName()
                                        : "Unknown"
                        ));

        List<LeaveReportResponse.LeaveTypeSummary>
                typeSummaries = byType.entrySet()
                        .stream()
                        .map(entry -> {

                            List<LeaveRequest> typeReqs =
                                    entry.getValue();

                            long approved = typeReqs.stream()
                                    .filter(r ->
                                            r.getStatus()
                                                    == LeaveRequest.LeaveStatus.APPROVED
                                                    || r.getStatus()
                                                    == LeaveRequest.LeaveStatus.AUTO_APPROVED
                                    )
                                    .count();

                            long rejected = typeReqs.stream()
                                    .filter(r ->
                                            r.getStatus()
                                                    == LeaveRequest.LeaveStatus.REJECTED
                                    )
                                    .count();

                            long pending = typeReqs.stream()
                                    .filter(r ->
                                            r.getStatus()
                                                    == LeaveRequest.LeaveStatus.PENDING
                                                    || r.getStatus()
                                                    == LeaveRequest.LeaveStatus.MANAGER_APPROVED
                                    )
                                    .count();

                            double totalDays = typeReqs.stream()
                                    .mapToDouble(
                                            r -> r.getTotalDays().doubleValue()
                                    )
                                    .sum();

                            return LeaveReportResponse.LeaveTypeSummary
                                    .builder()
                                    .leaveType(entry.getKey())
                                    .total(typeReqs.size())
                                    .approved(approved)
                                    .rejected(rejected)
                                    .pending(pending)
                                    .totalDays(totalDays)
                                    .build();
                        })
                        .collect(Collectors.toList());

        Map<UUID, List<LeaveRequest>> byEmployee =
                allRequests.stream()
                        .collect(Collectors.groupingBy(
                                r -> r.getEmployee().getId()
                        ));

        List<LeaveReportResponse.EmployeeLeaveSummary>
                employeeSummaries = byEmployee.entrySet()
                        .stream()
                        .map(entry -> {

                            List<LeaveRequest> empReqs =
                                    entry.getValue();

                            LeaveRequest first =
                                    empReqs.get(0);

                            String name = "";

                            if (first.getEmployee().getUser() != null) {
                                name = first.getEmployee()
                                        .getUser()
                                        .getFirstName()
                                        + " "
                                        + first.getEmployee()
                                        .getUser()
                                        .getLastName();
                            }

                            double totalDays = empReqs.stream()
                                    .mapToDouble(
                                            r -> r.getTotalDays().doubleValue()
                                    )
                                    .sum();

                            return LeaveReportResponse.EmployeeLeaveSummary
                                    .builder()
                                    .employeeId(entry.getKey())
                                    .name(name)
                                    .totalDays(totalDays)
                                    .requests(empReqs.size())
                                    .build();
                        })
                        .collect(Collectors.toList());

        return LeaveReportResponse.builder()
                .from(from)
                .to(to)
                .byType(typeSummaries)
                .employees(employeeSummaries)
                .build();
    }

    // ── Employee Report ────────────────────────────────────────────────────────

    public EmployeeReportResponse getEmployeeReport(
            UUID companyId) {

        List<Employee> all =
                employeeRepository.findAll()
                        .stream()
                        .filter(e -> !e.isDeleted())
                        .collect(Collectors.toList());

        long active = all.stream()
                .filter(e ->
                        e.getStatus()
                                == Employee.EmployeeStatus.ACTIVE
                )
                .count();

        long inactive = all.stream()
                .filter(e ->
                        e.getStatus()
                                != Employee.EmployeeStatus.ACTIVE
                )
                .count();

        Map<String, Long> byDepartment =
                all.stream()
                        .filter(e -> e.getDepartment() != null)
                        .collect(Collectors.groupingBy(
                                e -> e.getDepartment().getName(),
                                Collectors.counting()
                        ));

        Map<String, Long> byEmploymentType =
                all.stream()
                        .filter(e -> e.getEmploymentType() != null)
                        .collect(Collectors.groupingBy(
                                e -> e.getEmploymentType().name(),
                                Collectors.counting()
                        ));

        LocalDate thirtyDaysAgo =
                LocalDate.now().minusDays(30);

        long newHires30Days = all.stream()
                .filter(e ->
                        e.getDateOfJoining() != null
                                && !e.getDateOfJoining()
                                .isBefore(thirtyDaysAgo)
                )
                .count();

        long terminations30Days = all.stream()
                .filter(e ->
                        e.getDateOfLeaving() != null
                                && !e.getDateOfLeaving()
                                .isBefore(thirtyDaysAgo)
                )
                .count();

        return EmployeeReportResponse.builder()
                .total(all.size())
                .active(active)
                .inactive(inactive)
                .byDepartment(byDepartment)
                .byEmploymentType(byEmploymentType)
                .newHires30Days(newHires30Days)
                .terminations30Days(terminations30Days)
                .build();
    }

    // ── Project Report ─────────────────────────────────────────────────────────

    public ProjectReportResponse getProjectReport(
            UUID projectId) {

        Long totalLoggedMinutes =
                timeEntryRepository
                        .sumMinutesByProject(projectId)
                        .orElse(0L);

        String projectName =
                "Project " + projectId;

        Map<String, Long> tasksByStatus =
                new HashMap<>();

        long totalTasks = 0;
        long overdueTasks = 0;
        long totalEstimatedMinutes = 0;

        var projectOpt =
                projectRepository.findByIdAndDeletedFalse(
                        projectId
                );

        if (projectOpt.isPresent()) {
            projectName =
                    projectOpt.get().getName();
        }

        return ProjectReportResponse.builder()
                .projectId(projectId)
                .name(projectName)
                .tasksByStatus(tasksByStatus)
                .totalTasks(totalTasks)
                .overdueTasks(overdueTasks)
                .totalLoggedMinutes(totalLoggedMinutes)
                .totalEstimatedMinutes(totalEstimatedMinutes)
                .build();
    }
}