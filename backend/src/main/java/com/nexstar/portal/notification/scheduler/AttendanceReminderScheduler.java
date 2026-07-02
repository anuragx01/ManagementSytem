package com.nexstar.portal.notification.scheduler;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import com.nexstar.portal.attendance.repository.AttendanceRecordRepository;
import com.nexstar.portal.employee.entity.Employee;
import com.nexstar.portal.employee.repository.EmployeeRepository;
import com.nexstar.portal.notification.entity.Notification;
import com.nexstar.portal.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Sends attendance reminder notifications at 10:30 AM on weekdays
 * to employees who haven't clocked in yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceReminderScheduler {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 30 10 * * MON-FRI")
    @Transactional(readOnly = true)
    public void sendClockInReminders() {
        LocalDate today = LocalDate.now();
        log.info("Running attendance reminder job for {}", today);

        List<AttendanceRecord> todayRecords = attendanceRecordRepository.findByDateAndDeletedFalse(today);

        // Get IDs of employees who have clocked in or are on leave
        var attendedEmployeeIds = todayRecords.stream()
                .filter(r -> r.getClockIn() != null
                        || r.getStatus() == AttendanceRecord.AttendanceStatus.ON_LEAVE
                        || r.getStatus() == AttendanceRecord.AttendanceStatus.HOLIDAY
                        || r.getStatus() == AttendanceRecord.AttendanceStatus.WEEKEND)
                .map(r -> r.getEmployee().getId())
                .collect(java.util.stream.Collectors.toSet());

        // Notify active employees who haven't clocked in
        employeeRepository.findAll().stream()
                .filter(emp -> emp.getStatus() == Employee.EmployeeStatus.ACTIVE
                        && emp.getUser() != null
                        && emp.getUser().isActive()
                        && !attendedEmployeeIds.contains(emp.getId()))
                .forEach(emp -> {
                    try {
                        notificationService.createAndPush(
                                emp.getUser().getId(),
                                Notification.NotificationType.ATTENDANCE_REMINDER,
                                "Attendance Reminder",
                                "You haven't clocked in today. Please mark your attendance.",
                                "/attendance",
                                null,
                                null
                        );
                    } catch (Exception e) {
                        log.warn("Failed to send reminder to employee {}: {}", emp.getEmployeeId(), e.getMessage());
                    }
                });
    }
}
