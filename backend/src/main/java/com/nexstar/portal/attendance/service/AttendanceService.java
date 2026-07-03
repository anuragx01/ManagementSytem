package com.nexstar.portal.attendance.service;

import com.nexstar.portal.attendance.dto.*;
import com.nexstar.portal.attendance.entity.*;
import com.nexstar.portal.attendance.repository.*;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.organization.entity.Company;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceBreakRepository breakRepository;
    private final AttendancePolicyRepository policyRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    // ── Clock In ─────────────────────────────────────────────────────────────

    @Transactional
    public AttendanceRecordResponse clockIn(ClockInRequest request, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        LocalDate today = LocalDate.now();

        if (recordRepository.findOpenRecord(employee.getId(), today).isPresent()) {
            throw new BusinessException("Already clocked in today. Please clock out first.");
        }
        if (recordRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .map(r -> r.getClockOut() != null).orElse(false)) {
            throw new BusinessException("Already completed attendance for today.");
        }

        AttendancePolicy policy = resolvePolicy(employee);
        LocalDateTime now = LocalDateTime.now();

        // Calculate late minutes
        int lateMinutes = 0;
        LocalTime graceDeadline = policy.getOfficeStartTime().plusMinutes(policy.getGraceTimeMinutes());
        if (now.toLocalTime().isAfter(graceDeadline)) {
            lateMinutes = (int) Duration.between(
                    LocalDateTime.of(today, policy.getOfficeStartTime()), now).toMinutes();
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .date(today)
                .clockIn(now)
                .status(lateMinutes > 0
                        ? AttendanceRecord.AttendanceStatus.LATE
                        : AttendanceRecord.AttendanceStatus.PRESENT)
                .lateMinutes(lateMinutes)
                .workMode(request.getWorkMode())
                .clockInLocation(request.getLocation())
                .remarks(request.getRemarks())
                .policy(policy)
                .build();

        return toResponse(recordRepository.save(record));
    }

    // ── Clock Out ─────────────────────────────────────────────────────────────

    @Transactional
    public AttendanceRecordResponse clockOut(ClockOutRequest request, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        LocalDate today = LocalDate.now();

        AttendanceRecord record = recordRepository.findOpenRecord(employee.getId(), today)
                .orElseThrow(() -> new BusinessException("No active clock-in found for today."));

        // End any open break automatically
        breakRepository.findOpenBreak(record.getId()).ifPresent(b -> {
            b.setBreakEnd(LocalDateTime.now());
            b.setDurationMinutes((int) Duration.between(b.getBreakStart(), b.getBreakEnd()).toMinutes());
            breakRepository.save(b);
        });

        // Re-sum total break minutes
        int totalBreakMinutes = breakRepository
                .findByAttendanceRecordIdOrderByBreakStartAsc(record.getId()).stream()
                .filter(b -> b.getDurationMinutes() != null)
                .mapToInt(AttendanceBreak::getDurationMinutes).sum();

        LocalDateTime now = LocalDateTime.now();
        record.setClockOut(now);
        record.setClockOutLocation(request.getLocation());
        record.setTotalBreakMinutes(totalBreakMinutes);

        // Calculate worked minutes
        int grossMinutes = (int) Duration.between(record.getClockIn(), now).toMinutes();
        int workedMinutes = Math.max(0, grossMinutes - totalBreakMinutes);
        record.setWorkedMinutes(workedMinutes);

        AttendancePolicy policy = record.getPolicy();
        if (policy != null) {
            int fullDayMinutes = policy.getFullDayHours() * 60;
            int halfDayMinutes = policy.getHalfDayHours() * 60;

            // Early exit
            LocalTime officeEnd = policy.getOfficeEndTime();
            if (now.toLocalTime().isBefore(officeEnd)) {
                int earlyExit = (int) Duration.between(now.toLocalTime(), officeEnd).toMinutes();
                record.setEarlyExitMinutes(earlyExit);
            }

            // Overtime
            if (workedMinutes > fullDayMinutes + policy.getOvertimeThresholdMinutes()) {
                record.setOvertimeMinutes(workedMinutes - fullDayMinutes);
            }

            // Downgrade to HALF_DAY if worked < half day threshold
            if (workedMinutes < halfDayMinutes && record.getStatus() != AttendanceRecord.AttendanceStatus.ABSENT) {
                record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
            }
        }

        return toResponse(recordRepository.save(record));
    }

    // ── Break ─────────────────────────────────────────────────────────────────

    @Transactional
    public AttendanceRecordResponse startBreak(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        AttendanceRecord record = recordRepository.findOpenRecord(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new BusinessException("Not clocked in."));

        if (breakRepository.findOpenBreak(record.getId()).isPresent()) {
            throw new BusinessException("Already on a break.");
        }

        AttendanceBreak breakEntry = AttendanceBreak.builder()
                .attendanceRecord(record)
                .breakStart(LocalDateTime.now())
                .type(AttendanceBreak.BreakType.BREAK)
                .build();
        breakRepository.save(breakEntry);
        return toResponse(record);
    }

    @Transactional
    public AttendanceRecordResponse endBreak(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        AttendanceRecord record = recordRepository.findOpenRecord(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new BusinessException("Not clocked in."));

        AttendanceBreak openBreak = breakRepository.findOpenBreak(record.getId())
                .orElseThrow(() -> new BusinessException("No active break found."));

        LocalDateTime now = LocalDateTime.now();
        openBreak.setBreakEnd(now);
        openBreak.setDurationMinutes((int) Duration.between(openBreak.getBreakStart(), now).toMinutes());
        breakRepository.save(openBreak);

        // Update running total on record
        int totalBreak = breakRepository
                .findByAttendanceRecordIdOrderByBreakStartAsc(record.getId()).stream()
                .filter(b -> b.getDurationMinutes() != null)
                .mapToInt(AttendanceBreak::getDurationMinutes).sum();
        record.setTotalBreakMinutes(totalBreak);
        recordRepository.save(record);

        return toResponse(record);
    }

    // ── Today Status ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceRecordResponse getTodayStatus(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        AttendanceRecord record = recordRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElseGet(() -> AttendanceRecord.builder()
                        .employee(employee)
                        .date(LocalDate.now())
                        .status(AttendanceRecord.AttendanceStatus.ABSENT)
                        .build());
        return toResponse(record);
    }

    // ── My Attendance History ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<AttendanceRecordResponse> getMyAttendance(UserPrincipal currentUser, int page, int size) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        Page<AttendanceRecord> records = recordRepository.findByEmployeeIdAndDeletedFalse(
                employee.getId(), PageRequest.of(page, size, Sort.by("date").descending()));
        return PageResponse.of(records.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceRecordResponse> getAllAttendance(int page, int size) {
        Page<AttendanceRecord> records = recordRepository.findAllByDeletedFalse(
                PageRequest.of(page, size, Sort.by("date").descending()));
        return PageResponse.of(records.map(this::toResponse));
    }

    // ── Employee Attendance (HR/Manager) ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getEmployeeAttendance(
            UUID employeeId, LocalDate from, LocalDate to, UserPrincipal currentUser) {
        Employee requested = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        if (currentUser.getAuthorities().stream().anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()))) {
            Employee manager = getEmployeeByUser(currentUser.getId());
            if (requested.getReportingManager() == null || !requested.getReportingManager().getId().equals(manager.getId())) {
                throw new BusinessException("You can only view attendance for employees in your reporting scope.", HttpStatus.FORBIDDEN);
            }
        }
        return recordRepository.findByEmployeeAndDateRange(employeeId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceDashboardResponse getDashboard(LocalDate date) {
        if (date == null) date = LocalDate.now();

        long total     = employeeRepository.count();
        long present   = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.PRESENT);
        long late      = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.LATE);
        long onLeave   = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.ON_LEAVE);
        long wfh       = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.WORK_FROM_HOME);
        long weekend   = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.WEEKEND);
        long holiday   = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.HOLIDAY);
        long halfDay   = recordRepository.countByDateAndStatus(date, AttendanceRecord.AttendanceStatus.HALF_DAY);
        long presentCount = present + late + wfh + halfDay;
        long absent    = total - presentCount - onLeave - weekend - holiday;

        return AttendanceDashboardResponse.builder()
                .date(date)
                .totalEmployees(total)
                .present(presentCount)
                .late(late)
                .absent(Math.max(0, absent))
                .onLeave(onLeave)
                .workFromHome(wfh)
                .weekend(weekend)
                .holiday(holiday)
                .presentPercentage(total > 0 ? (presentCount * 100.0 / total) : 0)
                .absentPercentage(total > 0 ? (absent * 100.0 / total) : 0)
                .latePercentage(total > 0 ? (late * 100.0 / total) : 0)
                .build();
    }

    // ── Regularize (HR/Admin override) ───────────────────────────────────────

    @Transactional
    public AttendanceRecordResponse regularize(RegularizeRequest request) {
        AttendanceRecord record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record", "id", request.getRecordId()));

        if (request.getClockIn() != null)  record.setClockIn(request.getClockIn());
        if (request.getClockOut() != null) record.setClockOut(request.getClockOut());
        if (request.getStatus() != null)   record.setStatus(request.getStatus());

        if (record.getClockIn() != null && record.getClockOut() != null) {
            int grossMinutes = (int) Duration.between(record.getClockIn(), record.getClockOut()).toMinutes();
            record.setWorkedMinutes(Math.max(0, grossMinutes - record.getTotalBreakMinutes()));
        }

        record.setRemarks(request.getReason());
        record.setRegularized(true);
        return toResponse(recordRepository.save(record));
    }

    // ── Policy CRUD ───────────────────────────────────────────────────────────

    @Transactional
    public AttendancePolicyDto createPolicy(AttendancePolicyDto dto) {
        Company company = companyRepository.findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", dto.getCompanyId()));

        AttendancePolicy policy = AttendancePolicy.builder()
                .company(company)
                .name(dto.getName())
                .officeStartTime(dto.getOfficeStartTime())
                .officeEndTime(dto.getOfficeEndTime())
                .graceTimeMinutes(dto.getGraceTimeMinutes())
                .halfDayHours(dto.getHalfDayHours())
                .fullDayHours(dto.getFullDayHours())
                .overtimeThresholdMinutes(dto.getOvertimeThresholdMinutes())
                .workingDaysMask(dto.getWorkingDaysMask())
                .allowWorkFromHome(dto.isAllowWorkFromHome())
                .requireLocation(dto.isRequireLocation())
                .isDefault(dto.isDefault())
                .active(dto.isActive())
                .build();

        return toPolicyDto(policyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public List<AttendancePolicyDto> getPolicies(UUID companyId) {
        return policyRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toPolicyDto).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Employee getEmployeeByUser(UUID userId) {
        return employeeRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(
                        "Employee profile not found. Please contact HR.", HttpStatus.NOT_FOUND));
    }

    private AttendancePolicy resolvePolicy(Employee employee) {
        UUID companyId = companyRepository.findFirstByDeletedFalse()
                .map(c -> c.getId()).orElse(null);
        if (companyId == null) return defaultPolicy();
        return policyRepository.findByCompanyIdAndIsDefaultTrueAndDeletedFalse(companyId)
                .orElse(defaultPolicy());
    }

    private AttendancePolicy defaultPolicy() {
        // Fallback in-memory policy when none configured
        return AttendancePolicy.builder()
                .officeStartTime(LocalTime.of(9, 0))
                .officeEndTime(LocalTime.of(18, 0))
                .graceTimeMinutes(15)
                .halfDayHours(4)
                .fullDayHours(8)
                .overtimeThresholdMinutes(30)
                .workingDaysMask(62)
                .build();
    }

    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "0m";
        int h = minutes / 60;
        int m = minutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    AttendanceRecordResponse toResponse(AttendanceRecord r) {
        List<AttendanceRecordResponse.BreakDetail> breaks = List.of();
        if (r.getId() != null) {
            breaks = breakRepository.findByAttendanceRecordIdOrderByBreakStartAsc(r.getId())
                    .stream().map(b -> AttendanceRecordResponse.BreakDetail.builder()
                            .id(b.getId())
                            .breakStart(b.getBreakStart())
                            .breakEnd(b.getBreakEnd())
                            .durationMinutes(b.getDurationMinutes())
                            .type(b.getType() != null ? b.getType().name() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        Employee emp = r.getEmployee();
        String empName = emp.getUser() != null
                ? emp.getUser().getFirstName() + " " + emp.getUser().getLastName()
                : emp.getEmployeeId();

        return AttendanceRecordResponse.builder()
                .id(r.getId())
                .employeeId(emp.getId())
                .employeeName(empName)
                .employeeCode(emp.getEmployeeId())
                .date(r.getDate())
                .clockIn(r.getClockIn())
                .clockOut(r.getClockOut())
                .totalBreakMinutes(r.getTotalBreakMinutes())
                .workedMinutes(r.getWorkedMinutes() != null ? r.getWorkedMinutes() : 0)
                .lateMinutes(r.getLateMinutes())
                .earlyExitMinutes(r.getEarlyExitMinutes())
                .overtimeMinutes(r.getOvertimeMinutes())
                .status(r.getStatus())
                .workMode(r.getWorkMode())
                .clockInLocation(r.getClockInLocation())
                .clockOutLocation(r.getClockOutLocation())
                .remarks(r.getRemarks())
                .regularized(r.isRegularized())
                .breaks(breaks)
                .workedHours(formatMinutes(r.getWorkedMinutes() != null ? r.getWorkedMinutes() : 0))
                .lateBy(r.getLateMinutes() > 0 ? formatMinutes(r.getLateMinutes()) : null)
                .overtimeBy(r.getOvertimeMinutes() > 0 ? formatMinutes(r.getOvertimeMinutes()) : null)
                .build();
    }

    private AttendancePolicyDto toPolicyDto(AttendancePolicy p) {
        AttendancePolicyDto dto = new AttendancePolicyDto();
        dto.setId(p.getId());
        dto.setCompanyId(p.getCompany() != null ? p.getCompany().getId() : null);
        dto.setName(p.getName());
        dto.setOfficeStartTime(p.getOfficeStartTime());
        dto.setOfficeEndTime(p.getOfficeEndTime());
        dto.setGraceTimeMinutes(p.getGraceTimeMinutes());
        dto.setHalfDayHours(p.getHalfDayHours());
        dto.setFullDayHours(p.getFullDayHours());
        dto.setOvertimeThresholdMinutes(p.getOvertimeThresholdMinutes());
        dto.setWorkingDaysMask(p.getWorkingDaysMask());
        dto.setAllowWorkFromHome(p.isAllowWorkFromHome());
        dto.setRequireLocation(p.isRequireLocation());
        dto.setDefault(p.isDefault());
        dto.setActive(p.isActive());
        return dto;
    }
}
