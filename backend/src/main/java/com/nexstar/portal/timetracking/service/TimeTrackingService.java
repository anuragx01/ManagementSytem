package com.nexstar.portal.timetracking.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.security.UserPrincipal;
import com.nexstar.portal.timetracking.dto.*;
import com.nexstar.portal.timetracking.entity.TimeEntry;
import com.nexstar.portal.timetracking.entity.Timesheet;
import com.nexstar.portal.timetracking.repository.TimeEntryRepository;
import com.nexstar.portal.timetracking.repository.TimesheetRepository;
import com.nexstar.portal.projects.repository.ProjectRepository;
import com.nexstar.portal.tasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexstar.portal.common.response.PageResponse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeTrackingService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimesheetRepository timesheetRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // ── Timer Operations ──────────────────────────────────────────────────────

    public TimeEntryResponse startTimer(StartTimerRequest request, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());

        // Check no RUNNING timer exists
        Optional<TimeEntry> existingRunning = timeEntryRepository
                .findByEmployeeIdAndTimerStatusAndDeletedFalse(employee.getId(), TimeEntry.TimerStatus.RUNNING);
        if (existingRunning.isPresent()) {
            throw new BusinessException("You already have a running timer. Please stop it before starting a new one.");
        }

        // Check no PAUSED timer exists either
        Optional<TimeEntry> existingPaused = timeEntryRepository
                .findByEmployeeIdAndTimerStatusAndDeletedFalse(employee.getId(), TimeEntry.TimerStatus.PAUSED);
        if (existingPaused.isPresent()) {
            throw new BusinessException("You have a paused timer. Please stop it before starting a new one.");
        }

        LocalDateTime now = LocalDateTime.now();
        TimeEntry entry = TimeEntry.builder()
                .employee(employee)
                .description(request.getDescription())
                .startTime(now)
                .timerStatus(TimeEntry.TimerStatus.RUNNING)
                .billable(request.isBillable())
                .date(now.toLocalDate())
                .totalPausedMinutes(0)
                .build();

        if (request.getProjectId() != null) {
            setProjectOnEntry(entry, request.getProjectId());
        }
        if (request.getTaskId() != null) {
            setTaskOnEntry(entry, request.getTaskId());
        }

        return toResponse(timeEntryRepository.save(entry));
    }

    public TimeEntryResponse pauseTimer(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        TimeEntry entry = timeEntryRepository
                .findByEmployeeIdAndTimerStatusAndDeletedFalse(employee.getId(), TimeEntry.TimerStatus.RUNNING)
                .orElseThrow(() -> new BusinessException("No running timer found.", HttpStatus.NOT_FOUND));

        entry.setTimerStatus(TimeEntry.TimerStatus.PAUSED);
        entry.setPausedAt(LocalDateTime.now());
        return toResponse(timeEntryRepository.save(entry));
    }

    public TimeEntryResponse resumeTimer(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        TimeEntry entry = timeEntryRepository
                .findByEmployeeIdAndTimerStatusAndDeletedFalse(employee.getId(), TimeEntry.TimerStatus.PAUSED)
                .orElseThrow(() -> new BusinessException("No paused timer found.", HttpStatus.NOT_FOUND));

        if (entry.getPausedAt() != null) {
            long pausedMinutes = java.time.Duration.between(entry.getPausedAt(), LocalDateTime.now()).toMinutes();
            entry.setTotalPausedMinutes(entry.getTotalPausedMinutes() + (int) pausedMinutes);
        }
        entry.setTimerStatus(TimeEntry.TimerStatus.RUNNING);
        entry.setPausedAt(null);
        return toResponse(timeEntryRepository.save(entry));
    }

    public TimeEntryResponse stopTimer(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        TimeEntry entry = timeEntryRepository.findActiveTimerByEmployeeId(employee.getId())
                .orElseThrow(() -> new BusinessException("No active timer found.", HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // If it was paused, add remaining pause time first
        if (entry.getTimerStatus() == TimeEntry.TimerStatus.PAUSED && entry.getPausedAt() != null) {
            long pausedMinutes = java.time.Duration.between(entry.getPausedAt(), now).toMinutes();
            entry.setTotalPausedMinutes(entry.getTotalPausedMinutes() + (int) pausedMinutes);
        }

        entry.setEndTime(now);
        entry.setTimerStatus(TimeEntry.TimerStatus.STOPPED);

        long totalMinutes = java.time.Duration.between(entry.getStartTime(), now).toMinutes();
        int durationMinutes = (int) Math.max(0, totalMinutes - entry.getTotalPausedMinutes());
        entry.setDurationMinutes(durationMinutes);

        return toResponse(timeEntryRepository.save(entry));
    }

    public TimeEntryResponse getTimerStatus(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        Optional<TimeEntry> active = timeEntryRepository.findActiveTimerByEmployeeId(employee.getId());
        return active.map(this::toResponse).orElse(null);
    }

    // ── Manual Time Log ────────────────────────────────────────────────────────

    public TimeEntryResponse logTimeManually(TimeEntryRequest request, UserPrincipal currentUser) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("End time must be after start time.");
        }

        Employee employee;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findByIdAndDeletedFalse(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("Employee not found.", HttpStatus.NOT_FOUND));
        } else {
            employee = getEmployeeByUser(currentUser.getId());
        }

        long durationMinutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        TimeEntry entry = TimeEntry.builder()
                .employee(employee)
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes((int) durationMinutes)
                .billable(request.isBillable())
                .timerStatus(TimeEntry.TimerStatus.STOPPED)
                .date(request.getStartTime().toLocalDate())
                .totalPausedMinutes(0)
                .build();

        if (request.getProjectId() != null) {
            setProjectOnEntry(entry, request.getProjectId());
        }
        if (request.getTaskId() != null) {
            setTaskOnEntry(entry, request.getTaskId());
        }

        return toResponse(timeEntryRepository.save(entry));
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getMyTimeEntries(LocalDate from, LocalDate to, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().minusDays(7);
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        return timeEntryRepository
                .findByEmployeeIdAndDateBetweenAndDeletedFalse(employee.getId(), effectiveFrom, effectiveTo)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WeeklySummaryResponse getWeeklySummary(LocalDate weekStart, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());

        LocalDate effectiveWeekStart = weekStart != null
                ? weekStart
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate effectiveWeekEnd = effectiveWeekStart.plusDays(6);

        List<TimeEntry> entries = timeEntryRepository
                .findByEmployeeIdAndDateBetweenAndDeletedFalse(employee.getId(), effectiveWeekStart, effectiveWeekEnd);

        // Daily summary
        Map<DayOfWeek, Integer> dailySummary = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dailySummary.put(day, 0);
        }
        for (TimeEntry entry : entries) {
            if (entry.getDurationMinutes() != null) {
                dailySummary.merge(entry.getDate().getDayOfWeek(), entry.getDurationMinutes(), Integer::sum);
            }
        }

        int totalMinutes = entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();
        int billableMinutes = entries.stream()
                .filter(e -> e.getDurationMinutes() != null && e.isBillable())
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        // Project breakdown
        Map<UUID, WeeklySummaryResponse.ProjectBreakdown> projectMap = new LinkedHashMap<>();
        for (TimeEntry entry : entries) {
            if (entry.getProject() != null && entry.getDurationMinutes() != null) {
                UUID pid = entry.getProject().getId();
                projectMap.computeIfAbsent(pid, k -> WeeklySummaryResponse.ProjectBreakdown.builder()
                        .projectId(pid)
                        .projectName(entry.getProject().getName())
                        .minutes(0)
                        .billableMinutes(0)
                        .build());
                WeeklySummaryResponse.ProjectBreakdown bd = projectMap.get(pid);
                bd.setMinutes(bd.getMinutes() + entry.getDurationMinutes());
                if (entry.isBillable()) {
                    bd.setBillableMinutes(bd.getBillableMinutes() + entry.getDurationMinutes());
                }
            }
        }

        return WeeklySummaryResponse.builder()
                .weekStartDate(effectiveWeekStart)
                .weekEndDate(effectiveWeekEnd)
                .dailySummary(dailySummary)
                .totalMinutes(totalMinutes)
                .billableMinutes(billableMinutes)
                .projectBreakdown(new ArrayList<>(projectMap.values()))
                .build();
    }

    // ── Timesheet Operations ───────────────────────────────────────────────────

    public TimesheetResponse submitTimesheet(LocalDate weekStart, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());

        LocalDate effectiveWeekStart = weekStart != null
                ? weekStart
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate effectiveWeekEnd = effectiveWeekStart.plusDays(6);

        Timesheet timesheet = timesheetRepository
                .findByEmployeeIdAndWeekStartDateAndDeletedFalse(employee.getId(), effectiveWeekStart)
                .orElseGet(() -> Timesheet.builder()
                        .employee(employee)
                        .weekStartDate(effectiveWeekStart)
                        .weekEndDate(effectiveWeekEnd)
                        .status(Timesheet.TimesheetStatus.DRAFT)
                        .build());

        if (timesheet.getStatus() == Timesheet.TimesheetStatus.SUBMITTED
                || timesheet.getStatus() == Timesheet.TimesheetStatus.APPROVED) {
            throw new BusinessException("Timesheet has already been submitted or approved.");
        }

        List<TimeEntry> entries = timeEntryRepository
                .findByEmployeeIdAndDateBetweenAndDeletedFalse(employee.getId(), effectiveWeekStart, effectiveWeekEnd);

        int totalMinutes = entries.stream()
                .filter(e -> e.getDurationMinutes() != null)
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();
        int billableMinutes = entries.stream()
                .filter(e -> e.getDurationMinutes() != null && e.isBillable())
                .mapToInt(TimeEntry::getDurationMinutes)
                .sum();

        timesheet.setTotalMinutes(totalMinutes);
        timesheet.setBillableMinutes(billableMinutes);
        timesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(LocalDateTime.now());

        Timesheet saved = timesheetRepository.save(timesheet);

        // Update timesheetId on entries
        entries.forEach(e -> e.setTimesheetId(saved.getId()));
        timeEntryRepository.saveAll(entries);

        return toTimesheetResponse(saved, entries);
    }

    public TimesheetResponse approveTimesheet(UUID timesheetId, String remarks, boolean approve,
            UserPrincipal currentUser) {
        Timesheet timesheet = timesheetRepository.findByIdAndDeletedFalse(timesheetId)
                .orElseThrow(() -> new BusinessException("Timesheet not found.", HttpStatus.NOT_FOUND));

        if (timesheet.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new BusinessException("Only submitted timesheets can be approved or rejected.");
        }

        Employee approver = getEmployeeByUser(currentUser.getId());
        timesheet.setApprover(approver);
        timesheet.setApproverRemarks(remarks);
        timesheet.setApprovedAt(LocalDateTime.now());
        timesheet.setStatus(approve ? Timesheet.TimesheetStatus.APPROVED : Timesheet.TimesheetStatus.REJECTED);

        Timesheet saved = timesheetRepository.save(timesheet);
        List<TimeEntry> entries = timeEntryRepository.findByTimesheetIdAndDeletedFalse(saved.getId());
        return toTimesheetResponse(saved, entries);
    }

    @Transactional(readOnly = true)
    public List<TimesheetResponse> getPendingTimesheets(UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        List<Timesheet> timesheets = timesheetRepository
                .findByApproverIdAndStatusAndDeletedFalse(employee.getId(), Timesheet.TimesheetStatus.SUBMITTED);

        // Also get all submitted timesheets if no specific approver assigned
        if (timesheets.isEmpty()) {
            Page<Timesheet> page = timesheetRepository.findByStatusAndDeletedFalse(
                    Timesheet.TimesheetStatus.SUBMITTED, PageRequest.of(0, 100));
            timesheets = page.getContent();
        }

        return timesheets.stream()
                .map(ts -> {
                    List<TimeEntry> entries = timeEntryRepository.findByTimesheetIdAndDeletedFalse(ts.getId());
                    return toTimesheetResponse(ts, entries);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<TimesheetResponse> getMyTimesheets(int page, int size, UserPrincipal currentUser) {
        Employee employee = getEmployeeByUser(currentUser.getId());
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "weekStartDate"));
        Page<Timesheet> timesheets = timesheetRepository.findByEmployeeIdAndDeletedFalse(employee.getId(), pageable);

        Page<TimesheetResponse> responsePage = timesheets.map(ts -> {
            List<TimeEntry> entries = timeEntryRepository.findByTimesheetIdAndDeletedFalse(ts.getId());
            return toTimesheetResponse(ts, entries);
        });

        return PageResponse.of(responsePage);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Employee getEmployeeByUser(UUID userId) {
        return employeeRepository.findByUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException("Employee profile not found for this user.", HttpStatus.NOT_FOUND));
    }

    private String formatMinutes(int minutes) {
        if (minutes <= 0) return "0m";
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours == 0) return mins + "m";
        if (mins == 0) return hours + "h";
        return hours + "h " + mins + "m";
    }

    private void setProjectOnEntry(TimeEntry entry, UUID projectId) {
        var project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new BusinessException("Project not found.", HttpStatus.NOT_FOUND));
        entry.setProject(project);
    }

    private void setTaskOnEntry(TimeEntry entry, UUID taskId) {
        var task = taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new BusinessException("Task not found.", HttpStatus.NOT_FOUND));
        entry.setTask(task);
    }

    public TimeEntryResponse toResponse(TimeEntry entry) {
        String employeeName = null;
        if (entry.getEmployee() != null && entry.getEmployee().getUser() != null) {
            employeeName = entry.getEmployee().getUser().getFirstName()
                    + " " + entry.getEmployee().getUser().getLastName();
        }

        return TimeEntryResponse.builder()
                .id(entry.getId())
                .employeeId(entry.getEmployee() != null ? entry.getEmployee().getId() : null)
                .employeeName(employeeName)
                .projectId(entry.getProject() != null ? entry.getProject().getId() : null)
                .projectName(entry.getProject() != null ? entry.getProject().getName() : null)
                .taskId(entry.getTask() != null ? entry.getTask().getId() : null)
                .taskTitle(entry.getTask() != null ? entry.getTask().getTitle() : null)
                .taskKey(entry.getTask() != null ? entry.getTask().getTaskKey() : null)
                .description(entry.getDescription())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .durationMinutes(entry.getDurationMinutes())
                .durationFormatted(entry.getDurationMinutes() != null ? formatMinutes(entry.getDurationMinutes()) : null)
                .billable(entry.isBillable())
                .timerStatus(entry.getTimerStatus())
                .date(entry.getDate())
                .build();
    }

    private TimesheetResponse toTimesheetResponse(Timesheet ts, List<TimeEntry> entries) {
        String employeeName = null;
        if (ts.getEmployee() != null && ts.getEmployee().getUser() != null) {
            employeeName = ts.getEmployee().getUser().getFirstName()
                    + " " + ts.getEmployee().getUser().getLastName();
        }
        String approverName = null;
        if (ts.getApprover() != null && ts.getApprover().getUser() != null) {
            approverName = ts.getApprover().getUser().getFirstName()
                    + " " + ts.getApprover().getUser().getLastName();
        }

        return TimesheetResponse.builder()
                .id(ts.getId())
                .employeeId(ts.getEmployee() != null ? ts.getEmployee().getId() : null)
                .employeeName(employeeName)
                .weekStartDate(ts.getWeekStartDate())
                .weekEndDate(ts.getWeekEndDate())
                .totalMinutes(ts.getTotalMinutes())
                .billableMinutes(ts.getBillableMinutes())
                .totalFormatted(formatMinutes(ts.getTotalMinutes()))
                .billableFormatted(formatMinutes(ts.getBillableMinutes()))
                .status(ts.getStatus())
                .approverName(approverName)
                .approvedAt(ts.getApprovedAt())
                .submittedAt(ts.getSubmittedAt())
                .entries(entries.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }
}
