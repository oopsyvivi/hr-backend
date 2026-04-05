package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.OvertimeApplyRequest;
import com.hrms.hr_backend.dto.OvertimeDTO;
import com.hrms.hr_backend.service.OvertimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overtime")
@CrossOrigin(origins = "*")
public class OvertimeController {

    @Autowired
    private OvertimeService overtimeService;

    // POST apply overtime
    @PostMapping("/apply/{employeeId}")
    public ResponseEntity<?> applyOvertime(
        @PathVariable Long employeeId,
        @RequestBody OvertimeApplyRequest req
    ) {
        try {
            return ResponseEntity.ok(overtimeService.applyOvertime(employeeId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET my overtime
    @GetMapping("/my/{employeeId}")
    public ResponseEntity<List<OvertimeDTO>> getMyOvertime(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(overtimeService.getMyOvertime(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET all overtime (HR)
    @GetMapping("/all")
    public ResponseEntity<List<OvertimeDTO>> getAllOvertime() {
        return ResponseEntity.ok(overtimeService.getAllOvertime());
    }

    // PUT approve
    @PutMapping("/{overtimeId}/approve/{reviewerId}")
    public ResponseEntity<?> approve(
        @PathVariable Long overtimeId,
        @PathVariable Long reviewerId
    ) {
        try {
            return ResponseEntity.ok(overtimeService.approveOvertime(overtimeId, reviewerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT reject
    @PutMapping("/{overtimeId}/reject/{reviewerId}")
    public ResponseEntity<?> reject(
        @PathVariable Long overtimeId,
        @PathVariable Long reviewerId,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.getOrDefault("reason", "") : "";
            return ResponseEntity.ok(
                overtimeService.rejectOvertime(overtimeId, reviewerId, reason)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT cancel (employee)
    @PutMapping("/{overtimeId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long overtimeId) {
        try {
            return ResponseEntity.ok(overtimeService.cancelOvertime(overtimeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET monthly report (HR)
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(
        @RequestParam int year,
        @RequestParam int month
    ) {
        return ResponseEntity.ok(overtimeService.getMonthlyReport(year, month));
    }
}