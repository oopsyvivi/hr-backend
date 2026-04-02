package com.hrms.hr_backend.service;

import com.hrms.hr_backend.entity.LeaveType;
import com.hrms.hr_backend.repository.LeaveTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LeaveTypeService {

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findByIsActiveTrue();
    }

    public LeaveType createLeaveType(LeaveType leaveType) {
        if (leaveTypeRepository.existsByName(leaveType.getName())) {
            throw new RuntimeException("Leave type already exists: " + leaveType.getName());
        }
        leaveType.setIsActive(true);
        return leaveTypeRepository.save(leaveType);
    }

    public LeaveType updateLeaveType(Long id, LeaveType updated) {
        LeaveType lt = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave type not found"));
        lt.setName(updated.getName());
        lt.setDaysPerYear(updated.getDaysPerYear());
        lt.setDescription(updated.getDescription());
        lt.setCarryForward(updated.getCarryForward());
        return leaveTypeRepository.save(lt);
    }

    public void deleteLeaveType(Long id) {
        LeaveType lt = leaveTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave type not found"));
        lt.setIsActive(false);
        leaveTypeRepository.save(lt);
    }

    // Auto create defaults on startup
    public void initDefaultLeaveTypes() {
        Object[][] defaults = {
            {"Annual Leave",   14, "Yearly paid leave",            false},
            {"Sick Leave",      7, "Medical or health leave",      false},
            {"Casual Leave",    7, "Short personal leave",         false},
            {"Maternity Leave", 84,"Maternity leave for mothers",  false},
            {"Paternity Leave",  3,"Paternity leave for fathers",  false},
        };
        for (Object[] d : defaults) {
            if (!leaveTypeRepository.existsByName((String) d[0])) {
                LeaveType lt = new LeaveType();
                lt.setName((String) d[0]);
                lt.setDaysPerYear((Integer) d[1]);
                lt.setDescription((String) d[2]);
                lt.setCarryForward((Boolean) d[3]);
                lt.setIsActive(true);
                leaveTypeRepository.save(lt);
            }
        }
    }
}