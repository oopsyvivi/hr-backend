package com.hrms.hr_backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Data
public class LeaveApplyRequest {
    private Long leaveTypeId;
    private String leaveTypeName;
    private String dayType;
    private String halfPeriod;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Double daysCount;
    private String reason;
}