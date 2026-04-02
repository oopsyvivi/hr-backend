package com.hrms.hr_backend.service;

import com.hrms.hr_backend.entity.Department;
import com.hrms.hr_backend.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findByIsActiveTrue();
    }

    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department already exists: " + department.getName());
        }
        department.setIsActive(true);
        return departmentRepository.save(department);
    }

    public void initDefaultDepartments() {
        String[] defaults = {
            "Engineering", "HR", "Finance",
            "Marketing", "Design", "Operations"
        };
        for (String name : defaults) {
            if (!departmentRepository.existsByName(name)) {
                Department dept = new Department();
                dept.setName(name);
                dept.setIsActive(true);
                departmentRepository.save(dept);
            }
        }
    }

    // Update department
public Department updateDepartment(Long id, Department updated) {
    Department dept = departmentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Department not found"));
    dept.setName(updated.getName());
    return departmentRepository.save(dept);
}

// Soft delete department
public void deleteDepartment(Long id) {
    Department dept = departmentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Department not found"));
    dept.setIsActive(false);
    departmentRepository.save(dept);
}
}