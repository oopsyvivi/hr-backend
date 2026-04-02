package com.hrms.hr_backend.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
    private String email;
    private Long employeeId;
}



//```

//**What is a DTO?**
//```
//DTO = Data Transfer Object
//It is what React sends TO Java  →  LoginRequest
//It is what Java sends BACK to React  →  LoginResponse