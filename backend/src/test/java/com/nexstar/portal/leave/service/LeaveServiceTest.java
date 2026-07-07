package com.nexstar.portal.leave.service;

import com.nexstar.portal.attendance.repository.AttendanceRecordRepository;
import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.leave.dto.ApplyLeaveRequest;
import com.nexstar.portal.leave.dto.LeaveApprovalRequest;
import com.nexstar.portal.leave.dto.LeaveRequestResponse;
import com.nexstar.portal.leave.entity.LeaveBalance;
import com.nexstar.portal.leave.entity.LeaveRequest;
import com.nexstar.portal.leave.entity.LeaveType;
import com.nexstar.portal.leave.repository.LeaveBalanceRepository;
import com.nexstar.portal.leave.repository.LeaveRequestRepository;
import com.nexstar.portal.leave.repository.LeaveTypeRepository;
import com.nexstar.portal.notification.event.LeaveNotificationEvent;
import com.nexstar.portal.organization.entity.Company;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LeaveService leaveService;

    private UserPrincipal currentUser;
    private Employee employee;
    private LeaveType leaveType;
    private LeaveBalance leaveBalance;
    private UUID userId;
    private UUID employeeId;
    private UUID leaveTypeId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString(
                "00000000-0000-0000-0000-000000000001"
        );

        employeeId = UUID.fromString(
                "00000000-0000-0000-0000-000000000002"
        );

        leaveTypeId = UUID.fromString(
                "00000000-0000-0000-0000-000000000003"
        );

        currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .password("encoded")
                .build();

        employee = Employee.builder()
                .employeeId("EMP00001")
                .user(user)
                .build();

        Company company = Company.builder()
                .name("Test Company")
                .build();

        leaveType = LeaveType.builder()
                .company(company)
                .name("Annual Leave")
                .code("AL")
                .maxDaysPerYear(20)
                .paid(true)
                .active(true)
                .build();

        leaveBalance = LeaveBalance.builder()
                .employee(employee)
                .leaveType(leaveType)
                .year(2026)
                .allocatedDays(BigDecimal.valueOf(20.0))
                .usedDays(BigDecimal.ZERO)
                .pendingDays(BigDecimal.ZERO)
                .carriedForwardDays(BigDecimal.ZERO)
                .build();
    }

    @Test
    void applyLeave_withSufficientBalance_savesRequest() {
        // Arrange
        when(employeeRepository.findByUserIdAndDeletedFalse(userId))
                .thenReturn(Optional.of(employee));

        when(leaveTypeRepository.findByIdAndDeletedFalse(leaveTypeId))
                .thenReturn(Optional.of(leaveType));

        when(leaveRequestRepository.findOverlapping(
                any(),
                any(),
                any()
        )).thenReturn(Collections.emptyList());

        when(leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        any(),
                        any(),
                        anyInt()
                ))
                .thenReturn(Optional.of(leaveBalance));

        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Monday to Friday (5 working days)
        ApplyLeaveRequest request = new ApplyLeaveRequest();

        request.setLeaveTypeId(leaveTypeId);
        request.setStartDate(LocalDate.of(2026, 7, 6));
        request.setEndDate(LocalDate.of(2026, 7, 10));
        request.setDayType(LeaveRequest.DayType.FULL);
        request.setReason("Annual vacation");

        // Act
        LeaveRequestResponse response =
                leaveService.applyLeave(
                        request,
                        currentUser
                );

        // Assert
        verify(leaveRequestRepository)
                .save(any(LeaveRequest.class));

        verify(eventPublisher)
                .publishEvent(
                        any(LeaveNotificationEvent.class)
                );

        assertThat(response).isNotNull();
    }

    @Test
    void applyLeave_withInsufficientBalance_throwsException() {
        // Arrange
        LeaveBalance insufficientBalance =
                LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(leaveType)
                        .year(2026)
                        .allocatedDays(BigDecimal.valueOf(2.0))
                        .usedDays(BigDecimal.ZERO)
                        .pendingDays(BigDecimal.ZERO)
                        .carriedForwardDays(BigDecimal.ZERO)
                        .build();

        when(employeeRepository.findByUserIdAndDeletedFalse(userId))
                .thenReturn(Optional.of(employee));

        when(leaveTypeRepository.findByIdAndDeletedFalse(leaveTypeId))
                .thenReturn(Optional.of(leaveType));

        when(leaveRequestRepository.findOverlapping(
                any(),
                any(),
                any()
        )).thenReturn(Collections.emptyList());

        when(leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        any(),
                        any(),
                        anyInt()
                ))
                .thenReturn(Optional.of(insufficientBalance));

        // Request 5 working days but only 2 available
        ApplyLeaveRequest request = new ApplyLeaveRequest();

        request.setLeaveTypeId(leaveTypeId);
        request.setStartDate(LocalDate.of(2026, 7, 6));
        request.setEndDate(LocalDate.of(2026, 7, 10));
        request.setDayType(LeaveRequest.DayType.FULL);
        request.setReason("Vacation");

        // Act & Assert
        assertThatThrownBy(
                () -> leaveService.applyLeave(
                        request,
                        currentUser
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        "Insufficient leave balance"
                );
    }

    @Test
    void approveByManager_updatesStatusToManagerApproved() {
        // Arrange
        UUID managerUserId = UUID.fromString(
                "00000000-0000-0000-0000-000000000011"
        );

        UUID leaveRequestId = UUID.fromString(
                "00000000-0000-0000-0000-000000000020"
        );

        UserPrincipal managerPrincipal =
                mock(UserPrincipal.class);

        when(managerPrincipal.getId())
                .thenReturn(managerUserId);

        User managerUser = User.builder()
                .firstName("Manager")
                .lastName("User")
                .email("manager@test.com")
                .password("encoded")
                .build();

        Employee manager = Employee.builder()
                .employeeId("EMP00002")
                .user(managerUser)
                .build();

        LeaveRequest pendingRequest =
                LeaveRequest.builder()
                        .employee(employee)
                        .leaveType(leaveType)
                        .startDate(LocalDate.of(2026, 8, 1))
                        .endDate(LocalDate.of(2026, 8, 3))
                        .totalDays(BigDecimal.valueOf(3.0))
                        .dayType(LeaveRequest.DayType.FULL)
                        .reason("Vacation")
                        .status(LeaveRequest.LeaveStatus.PENDING)
                        .build();

        when(employeeRepository
                .findByUserIdAndDeletedFalse(managerUserId))
                .thenReturn(Optional.of(manager));

        when(leaveRequestRepository
                .findByIdAndDeletedFalse(leaveRequestId))
                .thenReturn(Optional.of(pendingRequest));

        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LeaveApprovalRequest approvalRequest =
                new LeaveApprovalRequest();

        approvalRequest.setLeaveRequestId(leaveRequestId);
        approvalRequest.setDecision(
                LeaveRequest.ApprovalDecision.APPROVED
        );
        approvalRequest.setRemarks("Approved");

        // Act
        LeaveRequestResponse response =
                leaveService.managerDecision(
                        approvalRequest,
                        managerPrincipal
                );

        // Assert
        ArgumentCaptor<LeaveRequest> captor =
                ArgumentCaptor.forClass(
                        LeaveRequest.class
                );

        verify(leaveRequestRepository)
                .save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(
                        LeaveRequest.LeaveStatus.MANAGER_APPROVED
                );

        assertThat(response).isNotNull();
    }

    @Test
    void calculateWorkingDays_excludesWeekends() {
        // Arrange
        when(employeeRepository.findByUserIdAndDeletedFalse(userId))
                .thenReturn(Optional.of(employee));

        when(leaveTypeRepository.findByIdAndDeletedFalse(leaveTypeId))
                .thenReturn(Optional.of(leaveType));

        when(leaveRequestRepository.findOverlapping(
                any(),
                any(),
                any()
        )).thenReturn(Collections.emptyList());

        when(leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYearAndDeletedFalse(
                        any(),
                        any(),
                        anyInt()
                ))
                .thenReturn(Optional.of(leaveBalance));

        ArgumentCaptor<LeaveRequest> captor =
                ArgumentCaptor.forClass(
                        LeaveRequest.class
                );

        when(leaveRequestRepository.save(captor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        when(leaveBalanceRepository.save(any(LeaveBalance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ApplyLeaveRequest request =
                new ApplyLeaveRequest();

        request.setLeaveTypeId(leaveTypeId);
        request.setStartDate(LocalDate.of(2026, 7, 6));
        request.setEndDate(LocalDate.of(2026, 7, 12));
        request.setDayType(LeaveRequest.DayType.FULL);
        request.setReason(
                "Testing weekend exclusion"
        );

        // Act
        leaveService.applyLeave(
                request,
                currentUser
        );

        // Assert
        assertThat(
                captor.getValue().getTotalDays()
        ).isEqualByComparingTo(
                BigDecimal.valueOf(5.0)
        );
    }
}