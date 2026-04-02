package com.hrms.hr_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String phone;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;

    @Column
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status = "Active";

    @Column(nullable = false)
    private String role = "EMPLOYEE";

    @Column
    private Double salary;

    @Column(nullable = false)
    private Boolean isActive = true;
}
