package com.hrms.hr_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String position;
    private Integer month;
    private Integer year;
    private Double basicSalary;
    private Double transportAllowance;
    private Double mealAllowance;
    private Double overtimeHours;
    private Double overtimePay;
    private Double grossSalary;
    private Double taxRate;
    private Double taxAmount;
    private Integer lateDays;
    private Double lateDeduction;
    private Integer absentDays;
    private Double absentDeduction;
    private Double totalDeductions;
    private Double netSalary;
    private Integer workingDays;
    private Integer presentDays;
    private String status;
    private LocalDateTime generatedAt;
    private LocalDateTime paidAt;
}