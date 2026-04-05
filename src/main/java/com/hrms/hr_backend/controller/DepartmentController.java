package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.entity.Department;
import com.hrms.hr_backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // GET all departments
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // POST create department
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        try {
            return ResponseEntity.ok(departmentService.createDepartment(department));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT update department
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
        @PathVariable Long id,
        @RequestBody Department department
    ) {
        try {
            return ResponseEntity.ok(departmentService.updateDepartment(id, department));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE department
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok("Department deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}