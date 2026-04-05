package com.hrms.hr_backend.controller;

import com.hrms.hr_backend.entity.LeaveType;
import com.hrms.hr_backend.service.LeaveTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin(origins = "*")
public class LeaveTypeController {

    @Autowired
    private LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAll() {
        return ResponseEntity.ok(leaveTypeService.getAllLeaveTypes());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LeaveType leaveType) {
        try {
            return ResponseEntity.ok(leaveTypeService.createLeaveType(leaveType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
        @PathVariable Long id,
        @RequestBody LeaveType leaveType
    ) {
        try {
            return ResponseEntity.ok(leaveTypeService.updateLeaveType(id, leaveType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            leaveTypeService.deleteLeaveType(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}