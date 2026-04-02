package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.AttendanceDTO;
import com.hrms.hr_backend.dto.ForgotRequestDTO;
import com.hrms.hr_backend.entity.*;
import com.hrms.hr_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

@Service
public class AttendanceService {

    @Autowired private AttendanceRepository    attendanceRepository;
    @Autowired private EmployeeRepository      employeeRepository;
    @Autowired private ForgotRequestRepository forgotRequestRepository;
    @Autowired private UserRepository          userRepository;
    @Autowired private WorkSettingsService     workSettingsService;

    //@Value("${work.start.time}") private String workStartTime;
    //@Value("${work.grace.period.minutes}") private int gracePeriodMinutes;

    // ── Check In ──
    public AttendanceDTO checkIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeAndWorkDate(employee, today)) {
            throw new RuntimeException("Already checked in today");
        }

        // Read settings from database
        WorkSettings ws = workSettingsService.getSettings();
        String[] parts = ws.getWorkStartTime().split(":");
        LocalTime startTime = LocalTime.of(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
        LocalTime lateAfter = startTime.plusMinutes(ws.getGracePeriodMins());

        LocalTime now = LocalTime.now();
        String status = "Incomplete"; // Wait for checkout

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setWorkDate(today);
        attendance.setClockIn(now);
        attendance.setStatus(status);
        attendance.setIsManualEntry(false);

        return toDTO(attendanceRepository.save(attendance));
    }
    // ── Check Out ──
    public AttendanceDTO checkOut(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
            .findByEmployeeAndWorkDate(employee, today)
            .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        if (attendance.getClockOut() != null) {
            throw new RuntimeException("Already checked out today");
        }

        // Read settings from database
        WorkSettings ws = workSettingsService.getSettings();
        String[] parts = ws.getWorkStartTime().split(":");
        LocalTime startTime = LocalTime.of(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1])
        );
        LocalTime lateAfter = startTime.plusMinutes(ws.getGracePeriodMins());

        LocalTime now = LocalTime.now();
        attendance.setClockOut(now);

        // Calculate work hours
        Duration duration = Duration.between(attendance.getClockIn(), now);
        double hours = duration.toMinutes() / 60.0;
        attendance.setWorkHours(Math.round(hours * 100.0) / 100.0);

        // Set final status
        if (attendance.getClockIn().isAfter(lateAfter)) {
            attendance.setStatus("Late");
        } else {
            attendance.setStatus("Present");
        }

        return toDTO(attendanceRepository.save(attendance));
    }

    // ── Get today's attendance for employee ──
    public AttendanceDTO getTodayAttendance(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        return attendanceRepository
            .findByEmployeeAndWorkDate(employee, LocalDate.now())
            .map(this::toDTO)
            .orElse(null);
    }

    // ── Get monthly attendance for employee ──
    public List<AttendanceDTO> getMyMonthlyAttendance(
        Long employeeId, int year, int month) {

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Get real attendance records
        List<Attendance> records = attendanceRepository
            .findByEmployeeAndMonth(employee, year, month);

        // Build map of existing records
        Map<LocalDate, AttendanceDTO> attendanceMap = new HashMap<>();
        for (Attendance a : records) {
            attendanceMap.put(a.getWorkDate(), toDTO(a));
        }

        // Fill in absent for shift days with no record
        LocalDate today = LocalDate.now();
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = LocalDate.of(year, month, d);

            // Skip future dates
            if (date.isAfter(today)) continue;

            // Skip weekends (default — override with shift calendar later)
            int dow = date.getDayOfWeek().getValue();
            if (dow == 6 || dow == 7) continue;

            // Skip if record exists
            if (attendanceMap.containsKey(date)) continue;

            // No record for a shift day → Absent
            AttendanceDTO absent = new AttendanceDTO();
            absent.setEmployeeId(employeeId);
            absent.setEmployeeName(employee.getFullName());
            absent.setDepartment(employee.getDepartment());
            absent.setWorkDate(date);
            absent.setClockIn(null);
            absent.setClockOut(null);
            absent.setStatus("Absent");
            absent.setWorkHours(null);

            attendanceMap.put(date, absent);
        }

        // Sort by date and return
        return attendanceMap.values().stream()
            .sorted(Comparator.comparing(AttendanceDTO::getWorkDate))
            .collect(Collectors.toList());
    }

    // ── Get all employees attendance for a date (HR) ──
    public List<AttendanceDTO> getDailyReport(LocalDate date) {
        return attendanceRepository
            .findByWorkDateOrderByEmployee_FullName(date)
            .stream().map(this::toDTO)
            .collect(Collectors.toList());
    }
 
    // ── Weekly Report (HR) ──
    public Map<String, Object> getWeeklyReport(int year, int week) {
        // Calculate week start and end dates
        LocalDate weekStart = LocalDate.ofYearDay(year, 1)
            .with(java.time.temporal.WeekFields.ISO.weekOfYear(), week)
            .with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Get all attendance for that week
        List<Attendance> records = attendanceRepository
            .findByWorkDateBetween(weekStart, weekEnd);

        // Get all active employees
        List<Employee> employees = employeeRepository.findByIsActiveTrue();

        // Build result
        List<Map<String, Object>> result = new ArrayList<>();

        for (Employee emp : employees) {
            Map<String, Object> empRow = new HashMap<>();
            empRow.put("employeeId",   emp.getId());
            empRow.put("employeeName", emp.getFullName());
            empRow.put("department",   emp.getDepartment());

            // For each day of the week
            List<Map<String, Object>> days = new ArrayList<>();
            for (int d = 0; d < 7; d++) {
                LocalDate date = weekStart.plusDays(d);
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());

                // Check if weekend
                int dow = date.getDayOfWeek().getValue();
                if (dow == 6 || dow == 7) {
                    dayData.put("status", "Weekend");
                } else {
                    // Find attendance record
                    records.stream()
                        .filter(r -> r.getEmployee().getId().equals(emp.getId())
                                && r.getWorkDate().equals(date))
                        .findFirst()
                        .ifPresentOrElse(
                            r -> {
                                dayData.put("status",   r.getStatus());
                                dayData.put("clockIn",  r.getClockIn()  != null ? r.getClockIn().toString()  : null);
                                dayData.put("clockOut", r.getClockOut() != null ? r.getClockOut().toString() : null);
                            },
                            () -> dayData.put("status",
                                date.isAfter(LocalDate.now()) ? "Future" : "Absent")
                        );
                }
                days.add(dayData);
            }
            empRow.put("days", days);
            result.add(empRow);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("weekStart", weekStart.toString());
        response.put("weekEnd",   weekEnd.toString());
        response.put("data",      result);
        return response;
    }

    // ── Monthly Summary Report (HR) ──
    public List<Map<String, Object>> getMonthlyReport(int year, int month) {
        List<Employee> employees = employeeRepository.findByIsActiveTrue();
        List<Attendance> records = attendanceRepository.findByMonth(year, month);

        // Count working days in month (weekdays only)
        int workingDays = 0;
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            int dow = LocalDate.of(year, month, d).getDayOfWeek().getValue();
            if (dow != 6 && dow != 7) workingDays++;
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Employee emp : employees) {
            List<Attendance> empRecords = records.stream()
                .filter(r -> r.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

            int present = (int) empRecords.stream().filter(r -> "Present".equals(r.getStatus())).count();
            int late    = (int) empRecords.stream().filter(r -> "Late".equals(r.getStatus())).count();
            int leave   = (int) empRecords.stream().filter(r -> "Leave".equals(r.getStatus())).count();
            int absent  = workingDays - present - late - leave;
            if (absent < 0) absent = 0;

            Map<String, Object> row = new HashMap<>();
            row.put("employeeId",   emp.getId());
            row.put("employeeName", emp.getFullName());
            row.put("department",   emp.getDepartment());
            row.put("present",      present);
            row.put("late",         late);
            row.put("absent",       absent);
            row.put("leave",        leave);
            row.put("workingDays",  workingDays);
            result.add(row);
        }

        return result;
    }


    // ── Submit forgot request ──
    public ForgotRequest submitForgotRequest(Long employeeId, ForgotRequestDTO dto) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        ForgotRequest request = new ForgotRequest();
        request.setEmployee(employee);
        request.setRequestDate(dto.getRequestDate());
        request.setMissingType(dto.getMissingType());
        request.setRequestedClockIn(dto.getRequestedClockIn());
        request.setRequestedClockOut(dto.getRequestedClockOut());
        request.setReason(dto.getReason());
        request.setStatus("Pending");
        request.setSubmittedAt(LocalDateTime.now());

        return forgotRequestRepository.save(request);
    }
   
    // ── Approve forgot request ──
    public ForgotRequest approveForgotRequest(Long requestId, Long reviewerId) {
        ForgotRequest request = forgotRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus("Approved");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        // Update or create attendance record
        updateAttendanceFromForgotRequest(request);

        return forgotRequestRepository.save(request);
    }

    // ── Reject forgot request ──
    public ForgotRequest rejectForgotRequest(Long requestId, Long reviewerId) {
        ForgotRequest request = forgotRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        request.setStatus("Rejected");
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        return forgotRequestRepository.save(request);
    }

    // ── Get all forgot requests (HR) ──
    public List<ForgotRequest> getAllForgotRequests() {
        return forgotRequestRepository.findAllByOrderBySubmittedAtDesc();
    }

    // ── Get pending forgot requests ──
    public List<ForgotRequest> getPendingForgotRequests() {
        return forgotRequestRepository.findByStatusOrderBySubmittedAtDesc("Pending");
    }

    // ── Helper: update attendance when forgot request approved ──
    private void updateAttendanceFromForgotRequest(ForgotRequest request) {
        Employee employee = request.getEmployee();
        LocalDate date    = request.getRequestDate();

        Attendance attendance = attendanceRepository
            .findByEmployeeAndWorkDate(employee, date)
            .orElseGet(() -> {
                Attendance a = new Attendance();
                a.setEmployee(employee);
                a.setWorkDate(date);
                a.setIsManualEntry(true);
                return a;
            });

        if (request.getRequestedClockIn() != null) {
            attendance.setClockIn(request.getRequestedClockIn());
        }
        if (request.getRequestedClockOut() != null) {
            attendance.setClockOut(request.getRequestedClockOut());
        }

        // Recalculate status and hours
        if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
            Duration duration = Duration.between(
                attendance.getClockIn(), attendance.getClockOut()
            );
            attendance.setWorkHours(duration.toMinutes() / 60.0);

            WorkSettings ws=workSettingsService.getSettings();
            String[] parts = ws.getWorkStartTime().split(":");
            LocalTime startTime  = LocalTime.of(
                Integer.parseInt(parts[0]), Integer.parseInt(parts[1])
            );
            LocalTime lateAfter  = startTime.plusMinutes(ws.getGracePeriodMins());
            attendance.setStatus(
                attendance.getClockIn().isAfter(lateAfter) ? "Late" : "Present"
            );
        }

        attendance.setIsManualEntry(true);
        attendanceRepository.save(attendance);
    }

    // ── Convert to DTO ──
    private AttendanceDTO toDTO(Attendance a) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFullName());
        dto.setDepartment(a.getEmployee().getDepartment());
        dto.setWorkDate(a.getWorkDate());
        dto.setClockIn(a.getClockIn());
        dto.setClockOut(a.getClockOut());
        dto.setStatus(a.getStatus());
        dto.setWorkHours(a.getWorkHours());
        dto.setIsManualEntry(a.getIsManualEntry());
        dto.setNote(a.getNote());
        return dto;
    }
}