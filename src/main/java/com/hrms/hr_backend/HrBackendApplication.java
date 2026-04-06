package com.hrms.hr_backend;

import com.hrms.hr_backend.entity.User;
import com.hrms.hr_backend.repository.UserRepository;
import com.hrms.hr_backend.service.DepartmentService;
import com.hrms.hr_backend.service.LeaveTypeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;;

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
    public CommandLineRunner initData(DepartmentService departmentService,LeaveTypeService leaveTypeService,UserRepository userRepository,PasswordEncoder passwordEncoder) {
        return args -> {
            departmentService.initDefaultDepartments();
            leaveTypeService.initDefaultLeaveTypes();
             // ── Auto create admin user if not exists ──
            if (!userRepository.existsByEmail("admin@hrms.com")) {
                User admin = new User();
                admin.setEmail("admin@hrms.com");
                admin.setPassword(passwordEncoder.encode("password123"));
                admin.setRole("ADMIN");
                admin.setFullName("System Admin");
                admin.setIsActive(true);
                userRepository.save(admin);
                System.out.println("✅ Admin user created!");
            } else {
                // Force reset admin password to make sure it's correct
                User admin = userRepository.findByEmail("admin@hrms.com").get();
                admin.setPassword(passwordEncoder.encode("password123"));
                userRepository.save(admin);
                System.out.println("✅ Admin password reset!");
            }
            System.out.println("✅ Default data initialized!");
        };
    }
}