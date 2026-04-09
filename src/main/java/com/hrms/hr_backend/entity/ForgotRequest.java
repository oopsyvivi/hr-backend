package com.hrms.hr_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "forgot_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ForgotRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Employee employee;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private String missingType; // Check In, Check Out, Both

    @Column
    private LocalTime requestedClockIn;

    @Column
    private LocalTime requestedClockOut;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status = "Pending"; // Pending, Approved, Rejected

    @Column
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private User reviewedBy;

    @Column
    private LocalDateTime reviewedAt;
}