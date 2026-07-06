package com.nexstar.portal.attendance.dto;

import com.nexstar.portal.attendance.entity.AttendanceRecord;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClockInRequest {
    private AttendanceRecord.WorkMode workMode;
    private String location;    // e.g. "28.6139,77.2090" or office name
    private String remarks;
}
