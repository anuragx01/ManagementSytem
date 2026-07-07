package com.nexstar.portal.leave.service;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import com.nexstar.portal.attendance.repository.AttendanceRecordRepository;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.common.response.PageResponse;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.leave.dto.*;
import com.nexstar.portal.leave.entity.*;
import com.nexstar.portal.leave.repository.*;
import com.nexstar.portal.organization.entity.Company;
import com.nexstar.portal.notification.event.LeaveNotificationEvent;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ── Leave Types ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeaveTypeDto> getLeaveTypes(UUID companyId) {
        return leaveTypeRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toLeaveTypeDto).collect(Collectors.toList());
    }

    @Transactional
    public LeaveTypeDto createLeaveType(LeaveTypeDto dto) {
        if (leaveTypeRepository.existsByNameAndCompanyIdAndDeletedFalse(
                dto.getName(), dto.getCompanyId())) {
            throw new BusinessException("Leave type with this name already exists");
        }

        Company company = companyRepository
                .findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company", "id", dto.getCompanyId()));

        LeaveType lt = LeaveType.builder()
                .company(company)
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .maxDaysPerYear(dto.getMaxDaysPerYear())
                .maxConsecutiveDays(dto.getMaxConsecutiveDays())
                .paid(dto.isPaid())
                .carryForwardAllowed(dto.isCarryForwardAllowed())
                .maxCarryForwardDays(dto.getMaxCarryForwardDays())
                .halfDayAllowed(dto.isHalfDayAllowed())
                .documentRequired(dto.isDocumentRequired())
                .minAdvanceDays(dto.getMinAdvanceDays())
                .color(dto.getColor())
                .active(dto.isActive())
                .build();

        return toLeaveTypeDto(leaveTypeRepository.save(lt));
    }

    @Transactional
    public LeaveTypeDto updateLeaveType(UUID id, LeaveTypeDto dto) {
        LeaveType lt = leaveTypeRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave type", "id", id));

        lt.setName(dto.getName());
        lt.setCode(dto.getCode());
        lt.setDescription(dto.getDescription());
        lt.setMaxDaysPerYear(dto.getMaxDaysPerYear());
        lt.setMaxConsecutiveDays(dto.getMaxConsecutiveDays());
        lt.setPaid(dto.isPaid());
        lt.setCarryForwardAllowed(dto.isCarryForwardAllowed());
        lt.setMaxCarryForwardDays(dto.getMaxCarryForwardDays());
        lt.setHalfDayAllowed(dto.isHalfDayAllowed());
        lt.setDocumentRequired(dto.isDocumentRequired());
        lt.setMinAdvanceDays(dto.getMinAdvanceDays());
        lt.setColor(dto.getColor());
        lt.setActive(dto.isActive());

        return toLeaveTypeDto(leaveTypeRepository.save(lt));
    }

    @Transactional
    public void deleteLeaveType(UUID id) {
        LeaveType lt = leaveTypeRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave type", "id", id));

        lt.setDeleted(true);
        leaveTypeRepository.save(lt);
    }

    // ── Leave Balance ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getMyBalance(
            UserPrincipal currentUser) {

        Employee employee = getEmployeeByUser(currentUser.getId());
        int year = Year.now().getValue();

        return leaveBalanceRepository
                .findByEmployeeAndYearWithType(employee.getId(), year)
                .stream()
                .map(this::toBalanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getEmployeeBalance(
            UUID employeeId,
            int year) {

        return leaveBalanceRepository
                .findByEmployeeAndYearWithType(employeeId, year)
                .stream()
                .map(this::toBalanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveBalanceResponse allocateLeave(
            UUID employeeId,
            UUID leaveTypeId,
            int year,
            double days) {

        Employee employee = employeeRepository
                .findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee", "id", employeeId));

        LeaveType leaveType = leaveTypeRepository
                .findByIdAndDeletedFalse(leaveTypeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave type", "id", leaveTypeId));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        employeeId,
                        leaveTypeId,
                        year
                )
                .orElse(
                        LeaveBalance.builder()
                                .employee(employee)
                                .leaveType(leaveType)
                                .year(year)
                                .build()
                );

        balance.setAllocatedDays(BigDecimal.valueOf(days));

        return toBalanceResponse(
                leaveBalanceRepository.save(balance)
        );
    }

    // ── Apply Leave ───────────────────────────────────────────────────────────

    @Transactional
    public LeaveRequestResponse applyLeave(
            ApplyLeaveRequest request,
            UserPrincipal currentUser) {

        Employee employee =
                getEmployeeByUser(currentUser.getId());

        LeaveType leaveType = leaveTypeRepository
                .findByIdAndDeletedFalse(request.getLeaveTypeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Leave type",
                                "id",
                                request.getLeaveTypeId()
                        ));

        if (request.getEndDate()
                .isBefore(request.getStartDate())) {
            throw new BusinessException(
                    "End date cannot be before start date");
        }

        List<LeaveRequest> overlapping =
                leaveRequestRepository.findOverlapping(
                        employee.getId(),
                        request.getStartDate(),
                        request.getEndDate()
                );

        if (!overlapping.isEmpty()) {
            throw new BusinessException(
                    "You already have a leave request overlapping these dates");
        }

        if (leaveType.getMinAdvanceDays() != null
                && leaveType.getMinAdvanceDays() > 0) {

            long daysUntilLeave =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            request.getStartDate()
                    );

            if (daysUntilLeave
                    < leaveType.getMinAdvanceDays()) {
                throw new BusinessException(
                        "This leave type requires at least "
                                + leaveType.getMinAdvanceDays()
                                + " days advance notice"
                );
            }
        }

        double totalDays = calculateWorkingDays(
                request.getStartDate(),
                request.getEndDate(),
                request.getDayType()
        );

        int year = request.getStartDate().getYear();

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        employee.getId(),
                        request.getLeaveTypeId(),
                        year
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "No leave balance allocated for this leave type"));

        if (balance.getAvailableDays()
                .compareTo(BigDecimal.valueOf(totalDays)) < 0) {

            throw new BusinessException(
                    String.format(
                            "Insufficient leave balance. Available: %.1f days, Requested: %.1f days",
                            balance.getAvailableDays().doubleValue(),
                            totalDays
                    )
            );
        }

        if (leaveType.isDocumentRequired()
                && (request.getDocumentUrl() == null
                || request.getDocumentUrl().isBlank())) {

            throw new BusinessException(
                    "This leave type requires a supporting document");
        }

        LeaveRequest.LeaveStatus initialStatus =
                leaveType.getName()
                        .equalsIgnoreCase("Work From Home")
                        ? LeaveRequest.LeaveStatus.AUTO_APPROVED
                        : LeaveRequest.LeaveStatus.PENDING;

        LeaveRequest leaveRequest =
                LeaveRequest.builder()
                        .employee(employee)
                        .leaveType(leaveType)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .totalDays(BigDecimal.valueOf(totalDays))
                        .dayType(request.getDayType())
                        .reason(request.getReason())
                        .documentUrl(request.getDocumentUrl())
                        .status(initialStatus)
                        .build();

        leaveRequest =
                leaveRequestRepository.save(leaveRequest);

        balance.setPendingDays(
                balance.getPendingDays()
                        .add(BigDecimal.valueOf(totalDays))
        );

        leaveBalanceRepository.save(balance);

        if (initialStatus
                == LeaveRequest.LeaveStatus.AUTO_APPROVED) {

            finalizeLeaveApproval(
                    leaveRequest,
                    balance,
                    BigDecimal.valueOf(totalDays)
            );
        }

        eventPublisher.publishEvent(
                new LeaveNotificationEvent(
                        this,
                        leaveRequest,
                        LeaveNotificationEvent.EventAction.APPLIED
                )
        );

        return toLeaveResponse(leaveRequest);
    }

    // ── Manager Approval ──────────────────────────────────────────────────────

    @Transactional
    public LeaveRequestResponse managerDecision(
            LeaveApprovalRequest request,
            UserPrincipal currentUser) {

        Employee manager =
                getEmployeeByUser(currentUser.getId());

        LeaveRequest leaveRequest =
                leaveRequestRepository
                        .findByIdAndDeletedFalse(
                                request.getLeaveRequestId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave request",
                                        "id",
                                        request.getLeaveRequestId()
                                ));

        if (leaveRequest.getStatus()
                != LeaveRequest.LeaveStatus.PENDING) {

            throw new BusinessException(
                    "Leave request is not in PENDING state");
        }

        leaveRequest.setManagerApprover(manager);
        leaveRequest.setManagerDecision(
                request.getDecision());
        leaveRequest.setManagerRemarks(
                request.getRemarks());
        leaveRequest.setManagerActionAt(
                LocalDateTime.now());

        if (request.getDecision()
                == LeaveRequest.ApprovalDecision.APPROVED) {

            leaveRequest.setStatus(
                    LeaveRequest.LeaveStatus.MANAGER_APPROVED);

        } else {

            leaveRequest.setStatus(
                    LeaveRequest.LeaveStatus.REJECTED);

            releasePendingBalance(leaveRequest);
        }

        LeaveRequest saved =
                leaveRequestRepository.save(leaveRequest);

        LeaveNotificationEvent.EventAction action =
                request.getDecision()
                        == LeaveRequest.ApprovalDecision.APPROVED
                        ? LeaveNotificationEvent.EventAction.MANAGER_APPROVED
                        : LeaveNotificationEvent.EventAction.MANAGER_REJECTED;

        eventPublisher.publishEvent(
                new LeaveNotificationEvent(
                        this,
                        saved,
                        action
                )
        );

        return toLeaveResponse(saved);
    }

    // ── HR Approval ───────────────────────────────────────────────────────────

    @Transactional
    public LeaveRequestResponse hrDecision(
            LeaveApprovalRequest request,
            UserPrincipal currentUser) {

        Employee hr =
                getEmployeeByUser(currentUser.getId());

        LeaveRequest leaveRequest =
                leaveRequestRepository
                        .findByIdAndDeletedFalse(
                                request.getLeaveRequestId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave request",
                                        "id",
                                        request.getLeaveRequestId()
                                ));

        if (leaveRequest.getStatus()
                != LeaveRequest.LeaveStatus.MANAGER_APPROVED) {

            throw new BusinessException(
                    "Leave request is not in MANAGER_APPROVED state");
        }

        leaveRequest.setHrApprover(hr);
        leaveRequest.setHrDecision(
                request.getDecision());
        leaveRequest.setHrRemarks(
                request.getRemarks());
        leaveRequest.setHrActionAt(
                LocalDateTime.now());

        if (request.getDecision()
                == LeaveRequest.ApprovalDecision.APPROVED) {

            leaveRequest.setStatus(
                    LeaveRequest.LeaveStatus.APPROVED);

            int year =
                    leaveRequest.getStartDate().getYear();

            LeaveBalance balance =
                    leaveBalanceRepository
                            .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                                    leaveRequest.getEmployee().getId(),
                                    leaveRequest.getLeaveType().getId(),
                                    year
                            )
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Leave balance not found"));

            finalizeLeaveApproval(
                    leaveRequest,
                    balance,
                    leaveRequest.getTotalDays()
            );

        } else {

            leaveRequest.setStatus(
                    LeaveRequest.LeaveStatus.REJECTED);

            releasePendingBalance(leaveRequest);
        }

        LeaveRequest saved =
                leaveRequestRepository.save(leaveRequest);

        LeaveNotificationEvent.EventAction action =
                request.getDecision()
                        == LeaveRequest.ApprovalDecision.APPROVED
                        ? LeaveNotificationEvent.EventAction.HR_APPROVED
                        : LeaveNotificationEvent.EventAction.HR_REJECTED;

        eventPublisher.publishEvent(
                new LeaveNotificationEvent(
                        this,
                        saved,
                        action
                )
        );

        return toLeaveResponse(saved);
    }

    // ── Cancel Leave ──────────────────────────────────────────────────────────

    @Transactional
    public LeaveRequestResponse cancelLeave(
            UUID leaveRequestId,
            String reason,
            UserPrincipal currentUser) {

        Employee employee =
                getEmployeeByUser(currentUser.getId());

        LeaveRequest leaveRequest =
                leaveRequestRepository
                        .findByIdAndDeletedFalse(leaveRequestId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Leave request",
                                        "id",
                                        leaveRequestId
                                ));

        if (!leaveRequest.getEmployee()
                .getId()
                .equals(employee.getId())) {

            throw new BusinessException(
                    "You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus()
                == LeaveRequest.LeaveStatus.REJECTED
                || leaveRequest.getStatus()
                == LeaveRequest.LeaveStatus.CANCELLED) {

            throw new BusinessException(
                    "Leave request is already "
                            + leaveRequest.getStatus());
        }

        if (leaveRequest.getStartDate()
                .isBefore(LocalDate.now())) {

            throw new BusinessException(
                    "Cannot cancel a leave that has already started");
        }

        LeaveRequest.LeaveStatus previousStatus =
                leaveRequest.getStatus();

        leaveRequest.setStatus(
                LeaveRequest.LeaveStatus.CANCELLED);

        leaveRequest.setCancelledByEmployee(true);
        leaveRequest.setCancellationReason(reason);

        if (previousStatus
                == LeaveRequest.LeaveStatus.APPROVED
                || previousStatus
                == LeaveRequest.LeaveStatus.AUTO_APPROVED) {

            int year =
                    leaveRequest.getStartDate().getYear();

            leaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                            employee.getId(),
                            leaveRequest.getLeaveType().getId(),
                            year
                    )
                    .ifPresent(b -> {

                        b.setUsedDays(
                                b.getUsedDays()
                                        .subtract(
                                                leaveRequest.getTotalDays())
                                        .max(BigDecimal.ZERO)
                        );

                        leaveBalanceRepository.save(b);
                    });

        } else {

            releasePendingBalance(leaveRequest);
        }

        LeaveRequest saved =
                leaveRequestRepository.save(leaveRequest);

        eventPublisher.publishEvent(
                new LeaveNotificationEvent(
                        this,
                        saved,
                        LeaveNotificationEvent.EventAction.CANCELLED
                )
        );

        return toLeaveResponse(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> getMyLeaves(
            UserPrincipal currentUser,
            int page,
            int size) {

        Employee employee =
                getEmployeeByUser(currentUser.getId());

        Page<LeaveRequest> result =
                leaveRequestRepository
                        .findByEmployeeIdAndDeletedFalse(
                                employee.getId(),
                                PageRequest.of(
                                        page,
                                        size,
                                        Sort.by("createdAt").descending()
                                )
                        );

        return PageResponse.of(
                result.map(this::toLeaveResponse));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingForManager(
            UserPrincipal currentUser) {

        Employee manager =
                getEmployeeByUser(currentUser.getId());

        return leaveRequestRepository
                .findPendingForManager(manager.getId())
                .stream()
                .map(this::toLeaveResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingForHr() {

        return leaveRequestRepository
                .findPendingForHr()
                .stream()
                .map(this::toLeaveResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getLeaveCalendar(
            LocalDate from,
            LocalDate to) {

        return leaveRequestRepository
                .findApprovedByDateRange(from, to)
                .stream()
                .map(this::toLeaveResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getTeamLeaveCalendar(
            UUID teamId,
            LocalDate from,
            LocalDate to) {

        return leaveRequestRepository
                .findApprovedByTeamAndDateRange(
                        teamId,
                        from,
                        to
                )
                .stream()
                .map(this::toLeaveResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Employee getEmployeeByUser(UUID userId) {

        return employeeRepository
                .findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Employee profile not found",
                                HttpStatus.NOT_FOUND
                        ));
    }

    private double calculateWorkingDays(
            LocalDate start,
            LocalDate end,
            LeaveRequest.DayType dayType) {

        if (dayType == LeaveRequest.DayType.HALF_FIRST
                || dayType == LeaveRequest.DayType.HALF_SECOND) {

            return 0.5;
        }

        long workingDays =
                start.datesUntil(end.plusDays(1))
                        .filter(d ->
                                d.getDayOfWeek()
                                        != DayOfWeek.SATURDAY
                                        && d.getDayOfWeek()
                                        != DayOfWeek.SUNDAY)
                        .count();

        return workingDays;
    }

    private void finalizeLeaveApproval(
            LeaveRequest lr,
            LeaveBalance balance,
            BigDecimal days) {

        BigDecimal daysValue = days;

        balance.setPendingDays(
                balance.getPendingDays()
                        .subtract(daysValue)
                        .max(BigDecimal.ZERO)
        );

        balance.setUsedDays(
                balance.getUsedDays()
                        .add(daysValue)
        );

        leaveBalanceRepository.save(balance);

        lr.getStartDate()
                .datesUntil(lr.getEndDate().plusDays(1))
                .forEach(date -> {

                    if (date.getDayOfWeek()
                            != DayOfWeek.SATURDAY
                            && date.getDayOfWeek()
                            != DayOfWeek.SUNDAY) {

                        AttendanceRecord record =
                                attendanceRecordRepository
                                        .findByEmployeeIdAndDate(
                                                lr.getEmployee().getId(),
                                                date
                                        )
                                        .orElse(
                                                AttendanceRecord.builder()
                                                        .employee(lr.getEmployee())
                                                        .date(date)
                                                        .build()
                                        );

                        record.setStatus(
                                AttendanceRecord.AttendanceStatus.ON_LEAVE);

                        attendanceRecordRepository.save(record);
                    }
                });
    }

    private void releasePendingBalance(
            LeaveRequest lr) {

        int year =
                lr.getStartDate().getYear();

        leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        lr.getEmployee().getId(),
                        lr.getLeaveType().getId(),
                        year
                )
                .ifPresent(b -> {

                    b.setPendingDays(
                            b.getPendingDays()
                                    .subtract(
                                            lr.getTotalDays())
                                    .max(BigDecimal.ZERO)
                    );

                    leaveBalanceRepository.save(b);
                });
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private LeaveTypeDto toLeaveTypeDto(
            LeaveType lt) {

        LeaveTypeDto dto =
                new LeaveTypeDto();

        dto.setId(lt.getId());
        dto.setCompanyId(
                lt.getCompany().getId());
        dto.setName(lt.getName());
        dto.setCode(lt.getCode());
        dto.setDescription(
                lt.getDescription());
        dto.setMaxDaysPerYear(
                lt.getMaxDaysPerYear());
        dto.setMaxConsecutiveDays(
                lt.getMaxConsecutiveDays());
        dto.setPaid(lt.isPaid());
        dto.setCarryForwardAllowed(
                lt.isCarryForwardAllowed());
        dto.setMaxCarryForwardDays(
                lt.getMaxCarryForwardDays());
        dto.setHalfDayAllowed(
                lt.isHalfDayAllowed());
        dto.setDocumentRequired(
                lt.isDocumentRequired());
        dto.setMinAdvanceDays(
                lt.getMinAdvanceDays());
        dto.setColor(lt.getColor());
        dto.setActive(lt.isActive());

        return dto;
    }

    private LeaveBalanceResponse toBalanceResponse(
            LeaveBalance b) {

        return LeaveBalanceResponse.builder()
                .id(b.getId())
                .leaveTypeId(
                        b.getLeaveType().getId())
                .leaveTypeName(
                        b.getLeaveType().getName())
                .leaveTypeColor(
                        b.getLeaveType().getColor())
                .paid(
                        b.getLeaveType().isPaid())
                .year(
                        b.getYear())
                .allocatedDays(
                        b.getAllocatedDays().doubleValue())
                .carriedForwardDays(
                        b.getCarriedForwardDays().doubleValue())
                .usedDays(
                        b.getUsedDays().doubleValue())
                .pendingDays(
                        b.getPendingDays().doubleValue())
                .availableDays(
                        b.getAvailableDays().doubleValue())
                .build();
    }

    LeaveRequestResponse toLeaveResponse(
            LeaveRequest lr) {

        Employee emp =
                lr.getEmployee();

        String empName =
                emp.getUser() != null
                        ? emp.getUser().getFirstName()
                        + " "
                        + emp.getUser().getLastName()
                        : emp.getEmployeeId();

        return LeaveRequestResponse.builder()
                .id(lr.getId())
                .employeeId(emp.getId())
                .employeeName(empName)
                .employeeCode(emp.getEmployeeId())
                .departmentName(
                        emp.getDepartment() != null
                                ? emp.getDepartment().getName()
                                : null)
                .leaveTypeId(
                        lr.getLeaveType().getId())
                .leaveTypeName(
                        lr.getLeaveType().getName())
                .leaveTypeColor(
                        lr.getLeaveType().getColor())
                .paid(
                        lr.getLeaveType().isPaid())
                .startDate(
                        lr.getStartDate())
                .endDate(
                        lr.getEndDate())
                .totalDays(
                        lr.getTotalDays().doubleValue())
                .dayType(
                        lr.getDayType())
                .reason(
                        lr.getReason())
                .documentUrl(
                        lr.getDocumentUrl())
                .status(
                        lr.getStatus())
                .managerId(
                        lr.getManagerApprover() != null
                                ? lr.getManagerApprover().getId()
                                : null)
                .managerName(
                        lr.getManagerApprover() != null
                                && lr.getManagerApprover().getUser() != null
                                ? lr.getManagerApprover()
                                        .getUser()
                                        .getFirstName()
                                        + " "
                                        + lr.getManagerApprover()
                                        .getUser()
                                        .getLastName()
                                : null)
                .managerDecision(
                        lr.getManagerDecision())
                .managerRemarks(
                        lr.getManagerRemarks())
                .managerActionAt(
                        lr.getManagerActionAt())
                .hrId(
                        lr.getHrApprover() != null
                                ? lr.getHrApprover().getId()
                                : null)
                .hrName(
                        lr.getHrApprover() != null
                                && lr.getHrApprover().getUser() != null
                                ? lr.getHrApprover()
                                        .getUser()
                                        .getFirstName()
                                        + " "
                                        + lr.getHrApprover()
                                        .getUser()
                                        .getLastName()
                                : null)
                .hrDecision(
                        lr.getHrDecision())
                .hrRemarks(
                        lr.getHrRemarks())
                .hrActionAt(
                        lr.getHrActionAt())
                .appliedAt(
                        lr.getCreatedAt())
                .build();
    }
}