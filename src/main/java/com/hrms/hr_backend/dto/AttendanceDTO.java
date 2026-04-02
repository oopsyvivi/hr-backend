package com.hrms.hr_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String department;
    private LocalDate workDate;
    private LocalTime clockIn;
    private LocalTime clockOut;
    private String status;
    private Double workHours;
    private Boolean isManualEntry;
    private String note;
}