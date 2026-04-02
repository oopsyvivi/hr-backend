package com.hrms.hr_backend;

import com.hrms.hr_backend.service.DepartmentService;
import com.hrms.hr_backend.service.LeaveTypeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(
    exclude = {UserDetailsServiceAutoConfiguration.class},
    scanBasePackages = "com.hrms.hr_backend"
)
public class HrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrBackendApplication.class, args);
    }

    // Auto-create departments when app starts
    @Bean
    public CommandLineRunner initData(DepartmentService departmentService,LeaveTypeService leaveTypeService) {
        return args -> {
            departmentService.initDefaultDepartments();
            leaveTypeService.initDefaultLeaveTypes();
            System.out.println("✅ Default data initialized!");
        };
    }
}