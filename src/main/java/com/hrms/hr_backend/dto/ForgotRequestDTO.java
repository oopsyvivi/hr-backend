package com.hrms.hr_backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ForgotRequestDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate requestDate;

    private String missingType;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime requestedClockIn;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime requestedClockOut;

    private String reason;
}