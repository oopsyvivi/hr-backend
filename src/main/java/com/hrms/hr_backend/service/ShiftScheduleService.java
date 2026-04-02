package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.ShiftDTO;
import com.hrms.hr_backend.entity.ShiftSchedule;
import com.hrms.hr_backend.entity.User;
import com.hrms.hr_backend.repository.ShiftScheduleRepository;
import com.hrms.hr_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ShiftScheduleService {

    @Autowired private ShiftScheduleRepository shiftScheduleRepository;
    @Autowired private UserRepository           userRepository;

    // ── Get shift status for a date ──
    public String getShiftStatus(LocalDate date) {
        return shiftScheduleRepository.findByShiftDate(date)
            .map(ShiftSchedule::getStatus)
            .orElseGet(() -> {
                // Default: weekday = shift, weekend = off
                int dow = date.getDayOfWeek().getValue();
                return (dow == 6 || dow == 7) ? "off" : "shift";
            });
    }

    // ── Get entire month as a map ──
    public Map<String, String> getMonthSchedule(int year, int month) {
        List<ShiftSchedule> schedules =
            shiftScheduleRepository.findByMonth(year, month);

        Map<String, String> result = new HashMap<>();

        // First fill defaults for all days
        int daysInMonth = LocalDate.of(year, month, 1)
            .lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = LocalDate.of(year, month, d);
            int dow = date.getDayOfWeek().getValue();
            String key = date.toString();
            result.put(key, (dow == 6 || dow == 7) ? "off" : "shift");
        }

        // Override with saved schedules
        for (ShiftSchedule s : schedules) {
            result.put(s.getShiftDate().toString(), s.getStatus());
        }

        return result;
    }

    // ── Save or update a single day ──
    public ShiftSchedule saveShiftDay(ShiftDTO dto, Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        ShiftSchedule schedule = shiftScheduleRepository
            .findByShiftDate(dto.getShiftDate())
            .orElse(new ShiftSchedule());

        schedule.setShiftDate(dto.getShiftDate());
        schedule.setStatus(dto.getStatus());
        schedule.setNote(dto.getNote());
        schedule.setCreatedBy(user);

        return shiftScheduleRepository.save(schedule);
    }

    // ── Bulk save entire month ──
    public void bulkSaveMonth(int year, int month,
                               String mode, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = LocalDate.of(year, month, d);
            int dow = date.getDayOfWeek().getValue();

            String status;
            if ("weekdays".equals(mode)) {
                status = (dow == 6 || dow == 7) ? "off" : "shift";
            } else if ("all_shift".equals(mode)) {
                status = "shift";
            } else {
                status = "off";
            }

            ShiftSchedule schedule = shiftScheduleRepository
                .findByShiftDate(date)
                .orElse(new ShiftSchedule());

            schedule.setShiftDate(date);
            schedule.setStatus(status);
            schedule.setCreatedBy(user);
            shiftScheduleRepository.save(schedule);
        }
    }
}