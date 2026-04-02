package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.PayrollDTO;
import com.hrms.hr_backend.dto.RunPayrollRequest;
import com.hrms.hr_backend.entity.*;
import com.hrms.hr_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired private PayrollRepository    payrollRepository;
    @Autowired private EmployeeRepository   employeeRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private OvertimeRepository   overtimeRepository;
    @Autowired private UserRepository       userRepository;

    // ── Run payroll for all employees ──
    public List<PayrollDTO> runPayroll(RunPayrollRequest req, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        List<Employee> employees = employeeRepository.findByIsActiveTrue();
        List<PayrollDTO> result  = new ArrayList<>();

        for (Employee emp : employees) {
            // Skip if already exists — update instead
            Payroll payroll = payrollRepository
                .findByEmployeeAndMonthAndYear(emp, req.getMonth(), req.getYear())
                .orElse(new Payroll());

            payroll.setEmployee(emp);
            payroll.setMonth(req.getMonth());
            payroll.setYear(req.getYear());

            // ── Basic salary ──
            double basicSalary = emp.getSalary() != null ? emp.getSalary() : 0.0;
            payroll.setBasicSalary(basicSalary);

            // ── Allowances ──
            payroll.setTransportAllowance(req.getTransportAllowance());
            payroll.setMealAllowance(req.getMealAllowance());

            // ── Attendance data ──
            List<Attendance> attendances = attendanceRepository
                .findByEmployeeAndMonth(emp, req.getYear(), req.getMonth());

            int presentDays = (int) attendances.stream()
                .filter(a -> "Present".equals(a.getStatus())).count();
            int lateDays    = (int) attendances.stream()
                .filter(a -> "Late".equals(a.getStatus())).count();

            // Working days in month (weekdays only)
            int workingDays = getWorkingDays(req.getYear(), req.getMonth());
            int leaveDays   = (int) attendances.stream()
                .filter(a -> "Leave".equals(a.getStatus())).count();
            int absentDays  = workingDays - presentDays - lateDays - leaveDays;
            if (absentDays < 0) absentDays = 0;

            payroll.setWorkingDays(workingDays);
            payroll.setPresentDays(presentDays + lateDays);
            payroll.setLateDays(lateDays);
            payroll.setAbsentDays(absentDays);

            // ── Overtime ──
            List<Overtime> overtimes = overtimeRepository
                .findApprovedByMonth(req.getYear(), req.getMonth())
                .stream()
                .filter(o -> o.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

            double otHours = overtimes.stream()
                .mapToDouble(Overtime::getHours).sum();
            double hourlyRate = basicSalary / (workingDays * 8.0);
            double otPay      = otHours * hourlyRate * req.getOvertimeRate();

            payroll.setOvertimeHours(Math.round(otHours * 10.0) / 10.0);
            payroll.setOvertimePay((double) Math.round(otPay));

            // ── Gross salary ──
            double gross = basicSalary
                + req.getTransportAllowance()
                + req.getMealAllowance()
                + otPay;
            payroll.setGrossSalary((double)Math.round(gross));

            // ── Deductions ──
            double tax          = gross * req.getTaxRate() / 100;
            double lateDeduct   = lateDays  * req.getLateDeductionPerDay();
            double absentDeduct = absentDays * req.getAbsentDeductionPerDay();
            double totalDeduct  = tax + lateDeduct + absentDeduct;

            payroll.setTaxRate(req.getTaxRate());
            payroll.setTaxAmount((double)Math.round(tax));
            payroll.setLateDeduction(lateDeduct);
            payroll.setAbsentDeduction(absentDeduct);
            payroll.setTotalDeductions((double)Math.round(totalDeduct));

            // ── Net salary ──
            payroll.setNetSalary((double)Math.round(gross - totalDeduct));

            // ── Meta ──
            payroll.setStatus("Draft");
            payroll.setGeneratedAt(LocalDateTime.now());
            payroll.setGeneratedBy(admin);

            result.add(toDTO(payrollRepository.save(payroll)));
        }

        return result;
    }

    // ── Get payroll for a month ──
    public List<PayrollDTO> getMonthlyPayroll(int month, int year) {
        return payrollRepository
            .findByMonthAndYearOrderByEmployee_FullName(month, year)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get my payroll (employee) ──
    public List<PayrollDTO> getMyPayroll(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        return payrollRepository
            .findByEmployeeOrderByYearDescMonthDesc(employee)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Get single payslip ──
    public PayrollDTO getPayslip(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
        return toDTO(payroll);
    }

    // ── Mark as paid ──
    public PayrollDTO markAsPaid(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setStatus("Paid");
        payroll.setPaidAt(LocalDateTime.now());
        return toDTO(payrollRepository.save(payroll));
    }

    // ── Mark all as paid for a month ──
    public List<PayrollDTO> markAllPaid(int month, int year) {
        List<Payroll> payrolls = payrollRepository
            .findByMonthAndYearOrderByEmployee_FullName(month, year);
        payrolls.forEach(p -> {
            p.setStatus("Paid");
            p.setPaidAt(LocalDateTime.now());
        });
        return payrollRepository.saveAll(payrolls)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }

    // ── Update employee salary ──
    public Employee updateSalary(Long employeeId, Double salary) {
        Employee emp = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        emp.setSalary(salary);
        return employeeRepository.save(emp);
    }

    // ── Monthly summary ──
    public Map<String, Object> getMonthlySummary(int month, int year) {
        List<Payroll> payrolls = payrollRepository
            .findByMonthAndYearOrderByEmployee_FullName(month, year);

        double totalGross = payrolls.stream().mapToDouble(p -> p.getGrossSalary() != null ? p.getGrossSalary() : 0).sum();
        double totalNet   = payrolls.stream().mapToDouble(p -> p.getNetSalary()   != null ? p.getNetSalary()   : 0).sum();
        double totalTax   = payrolls.stream().mapToDouble(p -> p.getTaxAmount()   != null ? p.getTaxAmount()   : 0).sum();
        long   paidCount  = payrolls.stream().filter(p -> "Paid".equals(p.getStatus())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("month",       month);
        summary.put("year",        year);
        summary.put("totalGross",  Math.round(totalGross));
        summary.put("totalNet",    Math.round(totalNet));
        summary.put("totalTax",    Math.round(totalTax));
        summary.put("paidCount",   paidCount);
        summary.put("totalCount",  payrolls.size());
        return summary;
    }

    // ── Helper — count working days in a month ──
    private int getWorkingDays(int year, int month) {
        int count = 0;
        int days  = LocalDate.of(year, month, 1).lengthOfMonth();
        for (int d = 1; d <= days; d++) {
            int dow = LocalDate.of(year, month, d).getDayOfWeek().getValue();
            if (dow != 6 && dow != 7) count++;
        }
        return count;
    }
    // ── Update status ──
    public PayrollDTO updateStatus(Long payrollId, String status) {
        Payroll payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setStatus(status);
        if ("Paid".equals(status)) {
            payroll.setPaidAt(LocalDateTime.now());
        }
        return toDTO(payrollRepository.save(payroll));
    }


    // ── Convert to DTO ──
    private PayrollDTO toDTO(Payroll p) {
        PayrollDTO dto = new PayrollDTO();
        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployee().getId());
        dto.setEmployeeName(p.getEmployee().getFullName());
        dto.setDepartment(p.getEmployee().getDepartment());
        dto.setPosition(p.getEmployee().getPosition());
        dto.setMonth(p.getMonth());
        dto.setYear(p.getYear());
        dto.setBasicSalary(p.getBasicSalary());
        dto.setTransportAllowance(p.getTransportAllowance());
        dto.setMealAllowance(p.getMealAllowance());
        dto.setOvertimeHours(p.getOvertimeHours());
        dto.setOvertimePay(p.getOvertimePay());
        dto.setGrossSalary(p.getGrossSalary());
        dto.setTaxRate(p.getTaxRate());
        dto.setTaxAmount(p.getTaxAmount());
        dto.setLateDays(p.getLateDays());
        dto.setLateDeduction(p.getLateDeduction());
        dto.setAbsentDays(p.getAbsentDays());
        dto.setAbsentDeduction(p.getAbsentDeduction());
        dto.setTotalDeductions(p.getTotalDeductions());
        dto.setNetSalary(p.getNetSalary());
        dto.setWorkingDays(p.getWorkingDays());
        dto.setPresentDays(p.getPresentDays());
        dto.setStatus(p.getStatus());
        dto.setGeneratedAt(p.getGeneratedAt());
        dto.setPaidAt(p.getPaidAt());
        return dto;
    }
}