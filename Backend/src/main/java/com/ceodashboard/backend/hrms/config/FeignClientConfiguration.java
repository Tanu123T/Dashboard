package com.ceodashboard.backend.hrms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Feign client configuration using Spring Cloud defaults.
 * Request/response handling is automatic with Spring Boot.
 */
@Configuration
@Import(HrmsFeignConfiguration.class)
public class FeignClientConfiguration {
    // Feign timeout and retry configuration is imported from HrmsFeignConfiguration.
}
