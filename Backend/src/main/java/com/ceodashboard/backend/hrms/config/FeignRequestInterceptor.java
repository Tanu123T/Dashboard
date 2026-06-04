package com.ceodashboard.backend.hrms.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    @Value("${hrms.api.auth-header:}")
    private String authHeader;

    @Autowired(required = false)
    private com.ceodashboard.backend.hrms.security.KeycloakTokenManager keycloakTokenManager;

    @Override
    public void apply(RequestTemplate template) {
        String header = null;

        if (keycloakTokenManager != null) {
            try {
                String token = keycloakTokenManager.getAccessToken();
                if (token != null && !token.isBlank()) {
                    header = "Bearer " + token;
                }
            } catch (Exception e) {
                logger.warn("Failed to acquire HRMS token from KeycloakTokenManager", e);
            }
        }

        if ((header == null || header.isEmpty()) && authHeader != null && !authHeader.isEmpty()) {
            if (authHeader.toLowerCase().startsWith("bearer ")) {
                header = authHeader;
            } else {
                header = "Bearer " + authHeader;
            }
        }

        if (header != null && !header.isEmpty()) {
            template.header("Authorization", header);
            logger.debug("Authorization header added to Feign request");
        }

        template.header("Content-Type", "application/json");
        template.header("Accept", "application/json");
        template.header("User-Agent", "CEO-Dashboard/1.0");
    }
}
