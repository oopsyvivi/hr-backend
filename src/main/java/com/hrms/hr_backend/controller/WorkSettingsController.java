package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.entity.WorkSettings;
import com.hrms.hr_backend.service.WorkSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class WorkSettingsController {

    @Autowired
    private WorkSettingsService workSettingsService;

    @GetMapping("/work-schedule")
    public ResponseEntity<WorkSettings> getSettings() {
        return ResponseEntity.ok(workSettingsService.getSettings());
    }

    @PostMapping("/work-schedule")
    public ResponseEntity<WorkSettings> updateSettings(
        @RequestBody WorkSettings settings
    ) {
        return ResponseEntity.ok(workSettingsService.updateSettings(settings));
    }
}