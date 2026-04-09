package com.hrms.hr_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column
    private LocalTime clockIn;

    @Column
    private LocalTime clockOut;

    @Column(nullable = false)
    private String status; // Present, Late, Absent, Leave, Incomplete

    @Column
    private Double workHours;

    @Column(nullable = false)
    private Boolean isManualEntry = false;

    @Column
    private String note;
}