package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.ShiftDTO;
import com.hrms.hr_backend.service.ShiftScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*")
public class ShiftScheduleController {

    @Autowired
    private ShiftScheduleService shiftScheduleService;

    // GET month schedule
    @GetMapping
    public ResponseEntity<?> getMonthSchedule(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(
            shiftScheduleService.getMonthSchedule(year, month)
        );
    }

    // POST save single day
    @PostMapping("/{userId}")
    public ResponseEntity<?> saveShiftDay(
        @RequestBody ShiftDTO dto,
        @PathVariable Long userId
    ) {
        try {
            return ResponseEntity.ok(
                shiftScheduleService.saveShiftDay(dto, userId)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST bulk set month
    @PostMapping("/bulk/{userId}")
    public ResponseEntity<?> bulkSet(
        @RequestParam int year,
        @RequestParam int month,
        @RequestParam String mode,
        @PathVariable Long userId
    ) {
        try {
            shiftScheduleService.bulkSaveMonth(year, month, mode, userId);
            return ResponseEntity.ok(
                Map.of("message", "Bulk update successful")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
