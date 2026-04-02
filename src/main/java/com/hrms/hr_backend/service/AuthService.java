package com.hrms.hr_backend.service;

import com.hrms.hr_backend.config.JwtUtil;
import com.hrms.hr_backend.dto.LoginRequest;
import com.hrms.hr_backend.dto.LoginResponse;
import com.hrms.hr_backend.entity.User;
import com.hrms.hr_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is disabled");
        }
  
        // 3. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 4. Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // 5. Return response
        return new LoginResponse(token, user.getRole(), user.getFullName(), user.getEmail(),user.getLinkedEmployeeId());
        
    }
}