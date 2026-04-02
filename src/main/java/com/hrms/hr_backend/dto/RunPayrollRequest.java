package com.hrms.hr_backend.dto;

import lombok.Data;

@Data
public class RunPayrollRequest {
    private Integer month;
    private Integer year;
    private Double  taxRate               = 5.0;
    private Double  overtimeRate          = 1.5;
    private Double  transportAllowance    = 30000.0;
    private Double  mealAllowance         = 20000.0;
    private Double  lateDeductionPerDay   = 5000.0;
    private Double  absentDeductionPerDay = 10000.0;
}