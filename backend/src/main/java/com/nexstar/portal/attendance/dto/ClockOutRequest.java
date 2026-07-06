package com.nexstar.portal.attendance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClockOutRequest {
    private String location;
    private String remarks;
}
