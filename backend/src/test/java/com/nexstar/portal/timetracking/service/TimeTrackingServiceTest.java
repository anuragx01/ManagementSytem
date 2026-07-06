package com.nexstar.portal.timetracking.service;

import com.nexstar.portal.authentication.entity.User;
import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.tasks.repository.TaskRepository;
import com.nexstar.portal.timetracking.dto.StartTimerRequest;
import com.nexstar.portal.timetracking.dto.TimeEntryResponse;
import com.nexstar.portal.timetracking.entity.TimeEntry;
import com.nexstar.portal.timetracking.repository.TimeEntryRepository;
import com.nexstar.portal.timetracking.repository.TimesheetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeTrackingServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TimeTrackingService timeTrackingService;

    private UserPrincipal currentUser;
    private Employee employee;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        currentUser = mock(UserPrincipal.class);
        when(currentUser.getId()).thenReturn(userId);

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@company.com")
                .password("encoded")
                .build();

        employee = Employee.builder()
                .employeeId("EMP00001")
                .user(user)
                .build();

        when(employeeRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Optional.of(employee));
    }

    @Test
    void startTimer_whenNoRunningTimer_createsEntry() {
        // Arrange
        when(timeEntryRepository.findByEmployeeIdAndTimerStatusAndDeletedFalse(
                any(UUID.class), eq(TimeEntry.TimerStatus.RUNNING))).thenReturn(Optional.empty());
        when(timeEntryRepository.findByEmployeeIdAndTimerStatusAndDeletedFalse(
                any(UUID.class), eq(TimeEntry.TimerStatus.PAUSED))).thenReturn(Optional.empty());

        ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
        when(timeEntryRepository.save(entryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        StartTimerRequest request = new StartTimerRequest();
        request.setDescription("Working on feature Y");
        request.setBillable(true);

        // Act
        TimeEntryResponse response = timeTrackingService.startTimer(request, currentUser);

        // Assert
        TimeEntry saved = entryCaptor.getValue();
        assertThat(saved.getTimerStatus()).isEqualTo(TimeEntry.TimerStatus.RUNNING);
        assertThat(saved.getStartTime()).isNotNull();
    }

    @Test
    void startTimer_whenTimerAlreadyRunning_throwsException() {
        // Arrange
        TimeEntry runningEntry = TimeEntry.builder()
                .employee(employee)
                .description("Already running")
                .startTime(LocalDateTime.now().minusMinutes(30))
                .timerStatus(TimeEntry.TimerStatus.RUNNING)
                .date(LocalDate.now())
                .totalPausedMinutes(0)
                .build();

        when(timeEntryRepository.findByEmployeeIdAndTimerStatusAndDeletedFalse(
                any(UUID.class), eq(TimeEntry.TimerStatus.RUNNING))).thenReturn(Optional.of(runningEntry));

        StartTimerRequest request = new StartTimerRequest();
        request.setDescription("New task");

        // Act & Assert
        assertThatThrownBy(() -> timeTrackingService.startTimer(request, currentUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already have a running timer");
    }

    @Test
    void stopTimer_calculatesCorrectDuration() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(90);

        TimeEntry runningEntry = TimeEntry.builder()
                .employee(employee)
                .description("Working on task")
                .startTime(startTime)
                .timerStatus(TimeEntry.TimerStatus.RUNNING)
                .date(LocalDate.now())
                .totalPausedMinutes(0)
                .build();

        when(timeEntryRepository.findActiveTimerByEmployeeId(any(UUID.class)))
                .thenReturn(Optional.of(runningEntry));

        ArgumentCaptor<TimeEntry> entryCaptor = ArgumentCaptor.forClass(TimeEntry.class);
        when(timeEntryRepository.save(entryCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TimeEntryResponse response = timeTrackingService.stopTimer(currentUser);

        // Assert
        TimeEntry saved = entryCaptor.getValue();
        assertThat(saved.getTimerStatus()).isEqualTo(TimeEntry.TimerStatus.STOPPED);
        // Duration should be approximately 90 minutes (allow ±2 min tolerance for test execution)
        assertThat(saved.getDurationMinutes()).isBetween(88, 92);
    }

    @Test
    void formatMinutes_returnsCorrectString() {
        // The formatMinutes method in TimeTrackingService is private, so we test it indirectly
        // via toResponse which exposes durationFormatted.
        // We verify the logic by stopping a timer with known duration.

        // 150 minutes = 2h 30m
        LocalDateTime startTime150 = LocalDateTime.now().minusMinutes(150);
        TimeEntry entry150 = TimeEntry.builder()
                .employee(employee)
                .description("150 min task")
                .startTime(startTime150)
                .timerStatus(TimeEntry.TimerStatus.RUNNING)
                .date(LocalDate.now())
                .totalPausedMinutes(0)
                .build();

        when(timeEntryRepository.findActiveTimerByEmployeeId(any(UUID.class)))
                .thenReturn(Optional.of(entry150));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> {
            TimeEntry e = inv.getArgument(0);
            // Simulate the stopped entry having 150 duration minutes
            e.setDurationMinutes(150);
            return e;
        });

        TimeEntryResponse response = timeTrackingService.stopTimer(currentUser);
        // durationFormatted for 150 min should be "2h 30m"
        assertThat(response.getDurationFormatted()).isEqualTo("2h 30m");
    }
}
