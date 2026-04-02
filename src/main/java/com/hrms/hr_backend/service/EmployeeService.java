package com.hrms.hr_backend.service;

import com.hrms.hr_backend.dto.EmployeeDTO;
import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.entity.User;
import com.hrms.hr_backend.repository.EmployeeRepository;
import com.hrms.hr_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Get all employees ──
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    // ── Get employee by ID ──
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    // ── Search employees ──
    public List<Employee> searchEmployees(String search) {
        return employeeRepository.searchEmployees(search);
    }

    // ── Get by department ──
    public List<Employee> getByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    // ── Create employee ──
    public Employee createEmployee(EmployeeDTO dto) {
        // Check if email already exists
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }

        // Auto generate code if not provided
        if (dto.getCode() == null || dto.getCode().isEmpty()) {
            long count = employeeRepository.count();
            dto.setCode(String.format("EMP-%03d", count + 1));
        }

        Employee employee = new Employee();
        employee.setCode(dto.getCode());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartment());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : "Active");
        employee.setRole(dto.getRole() != null ? dto.getRole() : "EMPLOYEE");
        employee.setSalary(dto.getSalary());
        employee.setIsActive(true);

        Employee savedEmployee=employeeRepository.save(employee);
        //Auto Create Login Account
        if(!userRepository.existsByEmail(dto.getEmail())){
            User user=new User();
            user.setEmail(dto.getEmail());
            user.setPassword(passwordEncoder.encode(savedEmployee.getCode()));
            user.setRole(dto.getRole() != null ? dto.getRole() :"EMPLOYEE");
            user.setFullName(dto.getFullName());
            user.setIsActive(true);
            user.setLinkedEmployeeId(savedEmployee.getId());
            userRepository.save(user);
        }
        return savedEmployee;
    }

    // ── Update employee ──
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = getEmployeeById(id);

        employee.setFullName(dto.getFullName());
        employee.setPhone(dto.getPhone());
        employee.setPosition(dto.getPosition());
        employee.setDepartment(dto.getDepartment());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());
        employee.setRole(dto.getRole());
        employee.setSalary(dto.getSalary());

        return employeeRepository.save(employee);
    }

    // ── Delete employee (soft delete) ──
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employee.setIsActive(false);    // soft delete — keeps record
        employee.setStatus("Inactive");
        employeeRepository.save(employee);
        //Also deactivate user account
        userRepository.findByEmail(employee.getEmail())
        .ifPresent(user ->{
            user.setIsActive(false);
            userRepository.save(user);
        });
    }
}