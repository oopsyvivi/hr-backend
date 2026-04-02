package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.dto.EmployeeDTO;
import com.hrms.hr_backend.entity.Employee;
import com.hrms.hr_backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // GET all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String department
    ) {
        try {
            List<Employee> employees;
            if (search != null && !search.isEmpty()) {
                employees = employeeService.searchEmployees(search);
            } else if (department != null && !department.isEmpty()) {
                employees = employeeService.getByDepartment(department);
            } else {
                employees = employeeService.getAllEmployees();
            }
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeeById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create employee
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeDTO dto) {
        try {
            Employee employee = employeeService.createEmployee(dto);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT update employee
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
        @PathVariable Long id,
        @RequestBody EmployeeDTO dto
    ) {
        try {
            Employee employee = employeeService.updateEmployee(id, dto);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE employee (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok("Employee deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}