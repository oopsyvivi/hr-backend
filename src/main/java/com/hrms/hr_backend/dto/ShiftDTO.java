package com.hrms.hr_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftDTO {
    private LocalDate shiftDate;
    private String status;
    private String note;
}