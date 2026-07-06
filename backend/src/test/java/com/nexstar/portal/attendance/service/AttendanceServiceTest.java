package com.nexstar.portal.attendance.service;

import com.nexstar.portal.attendance.dto.AttendanceRecordResponse;
import com.nexstar.portal.attendance.dto.ClockInRequest;
import com.nexstar.portal.attendance.dto.ClockOutRequest;
import com.nexstar.portal.attendance.entity.AttendancePolicy;
import com.nexstar.portal.attendance.entity.AttendanceRecord;
import com.nexstar.portal.attendance.repository.AttendanceBreakRepository;
import com.nexstar.portal.attendance.repository.AttendancePolicyRepository;
import com.nexstar.portal.attendance.repository.AttendanceRecordRepository;
import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.organization.repository.CompanyRepository;
import com.nexstar.portal.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRecordRepository recordRepository;

    @Mock
    private AttendanceBreakRepository breakRepository;

    @Mock
    private AttendancePolicyRepository policyRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private UserPrincipal currentUser;
    private Employee employee;
    private AttendancePolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID employeeId = UUID.fromString("00000000-0000-0000-0000-000000000002");

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

        // Inject ID via reflection workaround — use a spy or just rely on mocked save
        defaultPolicy = AttendancePolicy.builder()
                .officeStartTime(LocalTime.of(9, 0))
                .officeEndTime(LocalTime.of(18, 0))
                .graceTimeMinutes(15)
                .halfDayHours(4)
                .fullDayHours(8)
                .overtimeThresholdMinutes(30)
                .workingDaysMask(62)
                .build();

        when(employeeRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(employee));
    }

    @Test
    void clockIn_whenNoPreviousRecord_createsRecordWithPresentStatus() {
        // Arrange
        when(recordRepository.findOpenRecord(any(UUID.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(recordRepository.findByEmployeeIdAndDate(any(UUID.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(companyRepository.findFirstByDeletedFalse()).thenReturn(Optional.empty());
        when(recordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(breakRepository.findByAttendanceRecordIdOrderByBreakStartAsc(any()))
                .thenReturn(Collections.emptyList());

        ClockInRequest request = new ClockInRequest();
        request.setWorkMode(AttendanceRecord.WorkMode.OFFICE);

        // Act
        AttendanceRecordResponse response = attendanceService.clockIn(request, currentUser);

        // Assert
        verify(recordRepository).save(any(AttendanceRecord.class));
        assertThat(response.getClockIn()).isNotNull();
    }

    @Test
    void clockIn_whenAlreadyClockedIn_throwsException() {
        // Arrange
        AttendanceRecord existingRecord = AttendanceRecord.builder()
                .employee(employee)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(1))
                .status(AttendanceRecord.AttendanceStatus.PRESENT)
                .build();

        when(recordRepository.findOpenRecord(any(UUID.class), any(LocalDate.class)))
                .thenReturn(Optional.of(existingRecord));

        ClockInRequest request = new ClockInRequest();

        // Act & Assert
        assertThatThrownBy(() -> attendanceService.clockIn(request, currentUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Already clocked in");
    }

    @Test
    void clockOut_updatesWorkedMinutesAndStatus() {
        // Arrange
        LocalDateTime clockInTime = LocalDateTime.now().minusHours(9);

        AttendanceRecord existingRecord = AttendanceRecord.builder()
                .employee(employee)
                .date(LocalDate.now())
                .clockIn(clockInTime)
                .status(AttendanceRecord.AttendanceStatus.PRESENT)
                .policy(defaultPolicy)
                .build();

        when(recordRepository.findOpenRecord(any(UUID.class), any(LocalDate.class)))
                .thenReturn(Optional.of(existingRecord));
        when(breakRepository.findOpenBreak(any())).thenReturn(Optional.empty());
        when(breakRepository.findByAttendanceRecordIdOrderByBreakStartAsc(any()))
                .thenReturn(Collections.emptyList());
        when(recordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ClockOutRequest request = new ClockOutRequest();

        // Act
        AttendanceRecordResponse response = attendanceService.clockOut(request, currentUser);

        // Assert
        verify(recordRepository).save(any(AttendanceRecord.class));
        // After 9h of work the status should remain PRESENT (not downgraded to HALF_DAY)
        assertThat(response.getStatus()).isEqualTo(AttendanceRecord.AttendanceStatus.PRESENT);
    }
}
