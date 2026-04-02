package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.LeaveApplyRequest;
import com.hrms.hr_backend.dto.LeaveRequestDTO;
import com.hrms.hr_backend.entity.*;
import com.hrms.hr_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class LeaveService {

    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private EmployeeRepository     employeeRepository;
    @Autowired private LeaveTypeRepository    leaveTypeRepository;
    @Autowired private UserRepository         userRepository;

    // ── Apply for leave ──
    public LeaveRequestDTO applyLeave(Long employeeId, LeaveApplyRequest req) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(req.getLeaveTypeId())
            .orElseThrow(() -> new RuntimeException("Leave type not found"));

        // Check balance
        int year    = req.getStartDate().getYear();
        Double used = leaveRequestRepository.getTotalUsedDays(employee, leaveType, year);
        double remaining = leaveType.getDaysPerYear() - (used != null ? used : 0);

        if (req.getDaysCount() > remaining) {
            throw new RuntimeException(
                "Insufficient leave balance. Remaining: " + remaining + " days"
            );
        }

        // Check overlapping
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
            employee, req.getStartDate(), req.getEndDate()
        );
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("You already have a leave request for this period");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveType);
        leave.setDayType(req.getDayType());
        leave.setHalfPeriod(req.getHalfPeriod());
        leave.setStartDate(req.getStartDate());
        leave.setEndDate(req.getEndDate() != null ? req.getEndDate() : req.getStartDate());
        leave.setDaysCount(req.getDaysCount());
        leave.setReason(req.getReason());
        leave.setStatus("Pending");
        leave.setAppliedAt(LocalDateTime.now());

        return toDTO(leaveRequestRepository.save(leave));
    }

    // ── Get my leave requests ──
    public List<LeaveRequestDTO> getMyLeaves(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return leaveRequestRepository
            .findByEmployeeOrderByAppliedAtDesc(employee)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get all leave requests (HR) ──
    public List<LeaveRequestDTO> getAllLeaves() {
        return leaveRequestRepository
            .findAllByOrderByAppliedAtDesc()
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get pending requests ──
    public List<LeaveRequestDTO> getPendingLeaves() {
        return leaveRequestRepository
            .findByStatusOrderByAppliedAtDesc("Pending")
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Approve leave ──
    public LeaveRequestDTO approveLeave(Long leaveId, Long reviewerId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!"Pending".equals(leave.getStatus())) {
            throw new RuntimeException("Request is already " + leave.getStatus());
        }

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        leave.setStatus("Approved");
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());

        // Update attendance records for approved leave dates
        updateAttendanceForLeave(leave, true);

        return toDTO(leaveRequestRepository.save(leave));
    }

    // ── Reject leave ──
    public LeaveRequestDTO rejectLeave(Long leaveId, Long reviewerId, String reason) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        leave.setStatus("Rejected");
        leave.setRejectionReason(reason);
        leave.setReviewedBy(reviewer);
        leave.setReviewedAt(LocalDateTime.now());

        return toDTO(leaveRequestRepository.save(leave));
    }

    // ── Cancel leave (by employee) ──
    public LeaveRequestDTO cancelLeave(Long leaveId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!"Pending".equals(leave.getStatus())) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }

        leave.setStatus("Cancelled");
        return toDTO(leaveRequestRepository.save(leave));
    }

    // ── Get leave balance for employee ──
    public java.util.Map<Long, Double> getLeaveBalance(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        int year = LocalDate.now().getYear();
        java.util.Map<Long, Double> balance = new java.util.HashMap<>();

        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();
        for (LeaveType lt : leaveTypes) {
            Double used = leaveRequestRepository
                .getTotalUsedDays(employee, lt, year);
            double remaining = lt.getDaysPerYear() - (used != null ? used : 0);
            balance.put(lt.getId(), remaining);
        }
        return balance;
    }

    // ── Update attendance when leave approved ──
    @Autowired
    private AttendanceRepository attendanceRepository;

    private void updateAttendanceForLeave(LeaveRequest leave, boolean approve) {
        if (!approve) return;

        Employee emp   = leave.getEmployee();
        LocalDate date = leave.getStartDate();

        while (!date.isAfter(leave.getEndDate())) {
            int dow = date.getDayOfWeek().getValue();
            if (dow != 6 && dow != 7) {
                // Weekday — update or create attendance record
                LocalDate finalDate = date;
                com.hrms.hr_backend.entity.Attendance attendance =
                    attendanceRepository
                        .findByEmployeeAndWorkDate(emp, finalDate)
                        .orElseGet(() -> {
                            com.hrms.hr_backend.entity.Attendance a =
                                new com.hrms.hr_backend.entity.Attendance();
                            a.setEmployee(emp);
                            a.setWorkDate(finalDate);
                            a.setIsManualEntry(true);
                            return a;
                        });
                attendance.setStatus("Leave");
                attendance.setNote("Leave: " + leave.getLeaveType().getName());
                attendanceRepository.save(attendance);
            }
            date = date.plusDays(1);
        }
    }

    // ── Convert to DTO ──
    private LeaveRequestDTO toDTO(LeaveRequest l) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(l.getId());
        dto.setEmployeeId(l.getEmployee().getId());
        dto.setEmployeeName(l.getEmployee().getFullName());
        dto.setDepartment(l.getEmployee().getDepartment());
        dto.setLeaveTypeId(l.getLeaveType().getId());
        dto.setLeaveTypeName(l.getLeaveType().getName());
        dto.setDayType(l.getDayType());
        dto.setHalfPeriod(l.getHalfPeriod());
        dto.setStartDate(l.getStartDate());
        dto.setEndDate(l.getEndDate());
        dto.setDaysCount(l.getDaysCount());
        dto.setReason(l.getReason());
        dto.setStatus(l.getStatus());
        dto.setRejectionReason(l.getRejectionReason());
        dto.setAppliedAt(l.getAppliedAt());
        return dto;
    }

    // ── Get all employees leave balance (HR view) ──
    public List<Map<String, Object>> getAllEmployeesBalance() {
        List<Employee> employees = employeeRepository.findByIsActiveTrue();
        List<LeaveType> leaveTypes = leaveTypeRepository.findByIsActiveTrue();
        int year = LocalDate.now().getYear();

        List<Map<String, Object>> result = new ArrayList<>();

        for (Employee emp : employees) {
            Map<String, Object> row = new HashMap<>();
            row.put("employeeId",   emp.getId());
            row.put("employeeName", emp.getFullName());
            row.put("department",   emp.getDepartment());

            List<Map<String, Object>> balances = new ArrayList<>();
            int totalRemaining = 0;

            for (LeaveType lt : leaveTypes) {
                Double used = leaveRequestRepository
                    .getTotalUsedDays(emp, lt, year);
                double usedDays      = used != null ? used : 0;
                double remainingDays = lt.getDaysPerYear() - usedDays;
                totalRemaining += remainingDays;

                Map<String, Object> bal = new HashMap<>();
                bal.put("leaveTypeId",   lt.getId());
                bal.put("leaveTypeName", lt.getName());
                bal.put("total",         lt.getDaysPerYear());
                bal.put("used",          usedDays);
                bal.put("remaining",     remainingDays);
                balances.add(bal);
            }

            row.put("balances",       balances);
            row.put("totalRemaining", totalRemaining);
            result.add(row);
        }
        return result;
    }
}