package com.nexstar.portal.attendance.dto;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RegularizeRequest {

    @NotNull
    private UUID recordId;

    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private AttendanceRecord.AttendanceStatus status;

    @NotNull(message = "Reason is required for regularization")
    private String reason;
}
