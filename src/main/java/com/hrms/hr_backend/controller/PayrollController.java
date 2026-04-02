package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.PayrollDTO;
import com.hrms.hr_backend.dto.RunPayrollRequest;
import com.hrms.hr_backend.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin(origins = "http://localhost:5173")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // GET payroll for a month
    @GetMapping
    public ResponseEntity<List<PayrollDTO>> getMonthlyPayroll(
        @RequestParam int month,
        @RequestParam int year
    ) {
        return ResponseEntity.ok(payrollService.getMonthlyPayroll(month, year));
    }

    // GET my payroll (employee)
    @GetMapping("/my/{employeeId}")
    public ResponseEntity<?> getMyPayroll(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(payrollService.getMyPayroll(employeeId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET single payslip
    @GetMapping("/{payrollId}")
    public ResponseEntity<?> getPayslip(@PathVariable Long payrollId) {
        try {
            return ResponseEntity.ok(payrollService.getPayslip(payrollId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // POST run payroll
    @PostMapping("/run/{adminId}")
    public ResponseEntity<?> runPayroll(
        @RequestBody RunPayrollRequest req,
        @PathVariable Long adminId
    ) {
        try {
            return ResponseEntity.ok(payrollService.runPayroll(req, adminId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT mark as paid
    @PutMapping("/{payrollId}/paid")
    public ResponseEntity<?> markAsPaid(@PathVariable Long payrollId) {
        try {
            return ResponseEntity.ok(payrollService.markAsPaid(payrollId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT mark all paid
    @PutMapping("/paid-all")
    public ResponseEntity<?> markAllPaid(
        @RequestParam int month,
        @RequestParam int year
    ) {
        try {
            return ResponseEntity.ok(payrollService.markAllPaid(month, year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT update salary
    @PutMapping("/salary/{employeeId}")
    public ResponseEntity<?> updateSalary(
        @PathVariable Long employeeId,
        @RequestBody Map<String, Double> body
    ) {
        try {
            Double salary = body.get("salary");
            if (salary == null) return ResponseEntity.badRequest().body("Salary required");
            return ResponseEntity.ok(payrollService.updateSalary(employeeId, salary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET monthly summary
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(
        @RequestParam int month,
        @RequestParam int year
    ) {
        return ResponseEntity.ok(payrollService.getMonthlySummary(month, year));
    }
    // PUT update status
    @PutMapping("/{payrollId}/status")
    public ResponseEntity<?> updateStatus(
        @PathVariable Long payrollId,
        @RequestBody Map<String, String> body
    ) {
        try {
            String status = body.get("status");
            return ResponseEntity.ok(payrollService.updateStatus(payrollId, status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}