package com.hrms.hr_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "work_settings")
@Data
public class WorkSettings {

    @Id
    private Long id = 1L; // Only one row

    @Column(nullable = false)
    private String workStartTime = "09:00";

    @Column(nullable = false)
    private String workEndTime = "18:00";

    @Column(nullable = false)
    private Integer gracePeriodMins = 15;

    @Column(nullable = false)
    private Integer requiredHours = 8;

    @Column
    private String workDays = "1,2,3,4,5"; // Mon-Fri
}