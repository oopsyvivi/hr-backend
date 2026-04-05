package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.LeaveApplyRequest;
import com.hrms.hr_backend.dto.LeaveRequestDTO;
import com.hrms.hr_backend.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    // POST apply leave
    @PostMapping("/apply/{employeeId}")
    public ResponseEntity<?> applyLeave(
        @PathVariable Long employeeId,
        @RequestBody LeaveApplyRequest req
    ) {
        try {
            return ResponseEntity.ok(leaveService.applyLeave(employeeId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET my leaves
    @GetMapping("/my/{employeeId}")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaves(
        @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(leaveService.getMyLeaves(employeeId));
    }

    // GET all leaves (HR)
    @GetMapping("/all")
    public ResponseEntity<List<LeaveRequestDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // GET pending leaves
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequestDTO>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // GET leave balance
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<Map<Long, Double>> getBalance(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(leaveService.getLeaveBalance(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT approve leave
    @PutMapping("/{leaveId}/approve/{reviewerId}")
    public ResponseEntity<?> approveLeave(
        @PathVariable Long leaveId,
        @PathVariable Long reviewerId
    ) {
        try {
            return ResponseEntity.ok(leaveService.approveLeave(leaveId, reviewerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT reject leave
    @PutMapping("/{leaveId}/reject/{reviewerId}")
    public ResponseEntity<?> rejectLeave(
        @PathVariable Long leaveId,
        @PathVariable Long reviewerId,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.getOrDefault("reason", "") : "";
            return ResponseEntity.ok(leaveService.rejectLeave(leaveId, reviewerId, reason));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT cancel leave (employee)
    @PutMapping("/{leaveId}/cancel")
    public ResponseEntity<?> cancelLeave(@PathVariable Long leaveId) {
        try {
            return ResponseEntity.ok(leaveService.cancelLeave(leaveId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET all employees balance (HR)
    @GetMapping("/team-balance")
    public ResponseEntity<?> getTeamBalance() {
        try {
            return ResponseEntity.ok(leaveService.getAllEmployeesBalance());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}