package com.hrms.hr_backend.service;

import com.hrms.hr_backend.entity.WorkSettings;
import com.hrms.hr_backend.repository.WorkSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkSettingsService {

    @Autowired
    private WorkSettingsRepository workSettingsRepository;

    public WorkSettings getSettings() {
        return workSettingsRepository.findById(1L)
            .orElseGet(() -> {
                WorkSettings ws = new WorkSettings();
                ws.setId(1L);
                return workSettingsRepository.save(ws);
            });
    }

    public WorkSettings updateSettings(WorkSettings updated) {
        WorkSettings ws = getSettings();
        ws.setWorkStartTime(updated.getWorkStartTime());
        ws.setWorkEndTime(updated.getWorkEndTime());
        ws.setGracePeriodMins(updated.getGracePeriodMins());
        ws.setRequiredHours(updated.getRequiredHours());
        ws.setWorkDays(updated.getWorkDays());
        return workSettingsRepository.save(ws);
    }
}