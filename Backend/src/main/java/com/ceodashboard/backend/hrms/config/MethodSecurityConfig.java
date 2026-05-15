package com.ceodashboard.backend.hrms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
    // Enables @PreAuthorize and @PostAuthorize annotations for role-based access control
}
