package com.hrms.hr_backend.repository;

import com.hrms.hr_backend.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByIsActiveTrue();
    Boolean existsByName(String name);
}