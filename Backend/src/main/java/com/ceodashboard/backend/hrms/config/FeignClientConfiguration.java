package com.ceodashboard.backend.hrms.config;

import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration using Spring Cloud defaults.
 * Request/response handling is automatic with Spring Boot.
 */
@Configuration
public class FeignClientConfiguration {
    // Configuration is handled by FeignRequestInterceptor and FeignErrorDecoder
    // No additional codec configuration needed for Spring Boot 4.0.5
}
