package com.hrms.hr_backend.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig implements WebMvcConfigurer{

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS","PATCH")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
        
    }
}