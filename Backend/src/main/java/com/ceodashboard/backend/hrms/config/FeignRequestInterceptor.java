package com.ceodashboard.backend.hrms.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    @Value("${hrms.api.auth-header:}")
    private String authHeader;

    @Override
    public void apply(RequestTemplate template) {
        if (authHeader != null && !authHeader.isEmpty()) {
            template.header("Authorization", "Bearer " + authHeader);
            logger.debug("Authorization header added to Feign request");
        }

        // Add standard headers
        template.header("Content-Type", "application/json");
        template.header("Accept", "application/json");
        template.header("User-Agent", "CEO-Dashboard/1.0");
    }
}
