package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.WorkSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSettingsRepository extends JpaRepository<WorkSettings, Long> {
}