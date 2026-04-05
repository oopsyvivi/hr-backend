package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.AttendanceDTO;
import com.hrms.hr_backend.dto.ForgotRequestDTO;
import com.hrms.hr_backend.entity.ForgotRequest;
import com.hrms.hr_backend.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // POST check in
    @PostMapping("/checkin/{employeeId}")
    public ResponseEntity<?> checkIn(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(attendanceService.checkIn(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST check out
    @PostMapping("/checkout/{employeeId}")
    public ResponseEntity<?> checkOut(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET today's attendance
    @GetMapping("/today/{employeeId}")
    public ResponseEntity<?> getToday(@PathVariable Long employeeId) {
        try {
            AttendanceDTO dto = attendanceService.getTodayAttendance(employeeId);
            return ResponseEntity.ok(dto != null ? dto : Map.of("status", "not_checked_in"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET my monthly attendance
    @GetMapping("/my/{employeeId}")
    public ResponseEntity<?> getMyAttendance(
        @PathVariable Long employeeId,
        @RequestParam int year,
        @RequestParam int month
    ) {
        try {
            return ResponseEntity.ok(
                attendanceService.getMyMonthlyAttendance(employeeId, year, month)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET daily report (HR)
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date
    ) {
        return ResponseEntity.ok(attendanceService.getDailyReport(date));
    }

    // GET weekly report (HR)
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport(
        @RequestParam int year,
        @RequestParam int week
    ) {
        return ResponseEntity.ok(attendanceService.getWeeklyReport(year, week));
    }


    // GET monthly report (HR)
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(attendanceService.getMonthlyReport(year, month));
    }
    
    // POST forgot request
    @PostMapping("/forgot/{employeeId}")
    public ResponseEntity<?> submitForgot(
        @PathVariable Long employeeId,
        @RequestBody ForgotRequestDTO dto
    ) {
        try {
            return ResponseEntity.ok(
                attendanceService.submitForgotRequest(employeeId, dto)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET all forgot requests (HR)
    @GetMapping("/forgot")
    public ResponseEntity<List<ForgotRequest>> getAllForgot() {
        return ResponseEntity.ok(attendanceService.getAllForgotRequests());
    }

    // GET pending forgot requests
    @GetMapping("/forgot/pending")
    public ResponseEntity<List<ForgotRequest>> getPendingForgot() {
        return ResponseEntity.ok(attendanceService.getPendingForgotRequests());
    }

    // PUT approve forgot request
    @PutMapping("/forgot/{requestId}/approve/{reviewerId}")
    public ResponseEntity<?> approveForgot(
        @PathVariable Long requestId,
        @PathVariable Long reviewerId
    ) {
        try {
            return ResponseEntity.ok(
                attendanceService.approveForgotRequest(requestId, reviewerId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT reject forgot request
    @PutMapping("/forgot/{requestId}/reject/{reviewerId}")
    public ResponseEntity<?> rejectForgot(
        @PathVariable Long requestId,
        @PathVariable Long reviewerId
    ) {
        try {
            return ResponseEntity.ok(
                attendanceService.rejectForgotRequest(requestId, reviewerId)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
