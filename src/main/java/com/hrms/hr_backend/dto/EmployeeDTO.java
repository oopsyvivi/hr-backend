package com.hrms.hr_backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;
    private String code;
    private String fullName;
    private String email;
    private String phone;
    private String position;
    private String department;
    private LocalDate hireDate;
    private String status;
    private String role;
    private Double salary;
    private Boolean isActive;
}