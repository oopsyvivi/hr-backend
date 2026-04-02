package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.OvertimeApplyRequest;
import com.hrms.hr_backend.dto.OvertimeDTO;
import com.hrms.hr_backend.entity.*;
import com.hrms.hr_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OvertimeService {

    @Autowired private OvertimeRepository  overtimeRepository;
    @Autowired private EmployeeRepository  employeeRepository;
    @Autowired private UserRepository      userRepository;

    // ── Apply overtime ──
    public OvertimeDTO applyOvertime(Long employeeId, OvertimeApplyRequest req) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check duplicate
        if (overtimeRepository.existsByEmployeeAndDate(employee, req.getDate())) {
            throw new RuntimeException(
                "You already have an overtime request for " + req.getDate()
            );
        }

        // Validate times
        if (req.getStartTime().isAfter(req.getEndTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        // Calculate hours if not provided
        double hours = req.getHours() != null ? req.getHours() :
            Duration.between(req.getStartTime(), req.getEndTime()).toMinutes() / 60.0;

        if (hours <= 0 || hours > 12) {
            throw new RuntimeException("Invalid overtime hours: " + hours);
        }

        Overtime overtime = new Overtime();
        overtime.setEmployee(employee);
        overtime.setDate(req.getDate());
        overtime.setStartTime(req.getStartTime());
        overtime.setEndTime(req.getEndTime());
        overtime.setHours(Math.round(hours * 10.0) / 10.0);
        overtime.setReason(req.getReason());
        overtime.setStatus("Pending");
        overtime.setAppliedAt(LocalDateTime.now());

        return toDTO(overtimeRepository.save(overtime));
    }

    // ── Get my overtime ──
    public List<OvertimeDTO> getMyOvertime(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return overtimeRepository
            .findByEmployeeOrderByDateDesc(employee)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get all overtime (HR) ──
    public List<OvertimeDTO> getAllOvertime() {
        return overtimeRepository
            .findAllByOrderByAppliedAtDesc()
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Approve overtime ──
    public OvertimeDTO approveOvertime(Long overtimeId, Long reviewerId) {
        Overtime overtime = overtimeRepository.findById(overtimeId)
            .orElseThrow(() -> new RuntimeException("Overtime request not found"));

        if (!"Pending".equals(overtime.getStatus())) {
            throw new RuntimeException("Request is already " + overtime.getStatus());
        }

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        overtime.setStatus("Approved");
        overtime.setReviewedBy(reviewer);
        overtime.setReviewedAt(LocalDateTime.now());

        return toDTO(overtimeRepository.save(overtime));
    }

    // ── Reject overtime ──
    public OvertimeDTO rejectOvertime(Long overtimeId, Long reviewerId, String reason) {
        Overtime overtime = overtimeRepository.findById(overtimeId)
            .orElseThrow(() -> new RuntimeException("Overtime request not found"));

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        overtime.setStatus("Rejected");
        overtime.setRejectionReason(reason);
        overtime.setReviewedBy(reviewer);
        overtime.setReviewedAt(LocalDateTime.now());

        return toDTO(overtimeRepository.save(overtime));
    }

    // ── Cancel overtime ──
    public OvertimeDTO cancelOvertime(Long overtimeId) {
        Overtime overtime = overtimeRepository.findById(overtimeId)
            .orElseThrow(() -> new RuntimeException("Overtime request not found"));

        if (!"Pending".equals(overtime.getStatus())) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        overtime.setStatus("Cancelled");
        return toDTO(overtimeRepository.save(overtime));
    }

    // ── Monthly report (HR) ──
    public List<Map<String, Object>> getMonthlyReport(int year, int month) {
        List<Employee> employees = employeeRepository.findByIsActiveTrue();
        List<Overtime> allRecords = overtimeRepository.findByMonth(year, month);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Employee emp : employees) {
            List<Overtime> empRecords = allRecords.stream()
                .filter(o -> o.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

            long approvedCount = empRecords.stream()
                .filter(o -> "Approved".equals(o.getStatus()))
                .count();

            long pendingCount  = empRecords.stream()
                .filter(o -> "Pending".equals(o.getStatus()))
                .count();

            double totalHours  = empRecords.stream()
                .filter(o -> "Approved".equals(o.getStatus()))
                .mapToDouble(Overtime::getHours)
                .sum();

            Map<String, Object> row = new HashMap<>();
            row.put("employeeId",    emp.getId());
            row.put("employeeName",  emp.getFullName());
            row.put("department",    emp.getDepartment());
            row.put("totalHours",    Math.round(totalHours * 10.0) / 10.0);
            row.put("approvedCount", approvedCount);
            row.put("pendingCount",  pendingCount);
            result.add(row);
        }

        return result;
    }

    // ── Convert to DTO ──
    private OvertimeDTO toDTO(Overtime o) {
        OvertimeDTO dto = new OvertimeDTO();
        dto.setId(o.getId());
        dto.setEmployeeId(o.getEmployee().getId());
        dto.setEmployeeName(o.getEmployee().getFullName());
        dto.setDepartment(o.getEmployee().getDepartment());
        dto.setDate(o.getDate());
        dto.setStartTime(o.getStartTime());
        dto.setEndTime(o.getEndTime());
        dto.setHours(o.getHours());
        dto.setReason(o.getReason());
        dto.setStatus(o.getStatus());
        dto.setRejectionReason(o.getRejectionReason());
        dto.setAppliedAt(o.getAppliedAt());
        return dto;
    }
}