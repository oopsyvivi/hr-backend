package com.hrms.hr_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "payroll")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Employee employee;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    // ── Earnings ──
    @Column(nullable = false)
    private Double basicSalary;

    @Column(nullable = false)
    private Double transportAllowance = 0.0;

    @Column(nullable = false)
    private Double mealAllowance = 0.0;

    @Column(nullable = false)
    private Double overtimeHours = 0.0;

    @Column(nullable = false)
    private Double overtimePay = 0.0;

    @Column(nullable = false)
    private Double grossSalary = 0.0;

    // ── Deductions ──
    @Column(nullable = false)
    private Double taxRate = 5.0;

    @Column(nullable = false)
    private Double taxAmount = 0.0;

    @Column(nullable = false)
    private Integer lateDays = 0;

    @Column(nullable = false)
    private Double lateDeduction = 0.0;

    @Column(nullable = false)
    private Integer absentDays = 0;

    @Column(nullable = false)
    private Double absentDeduction = 0.0;

    @Column(nullable = false)
    private Double totalDeductions = 0.0;

    // ── Net ──
    @Column(nullable = false)
    private Double netSalary = 0.0;

    // ── Attendance ──
    @Column(nullable = false)
    private Integer workingDays = 22;

    @Column(nullable = false)
    private Integer presentDays = 0;

    // ── Status ──
    @Column(nullable = false)
    private String status = "Draft"; // Draft | Pending | Paid

    @Column
    private LocalDateTime generatedAt;

    @Column
    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "generated_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private User generatedBy;
}