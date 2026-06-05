package com.ceodashboard.backend.hrms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * DEPRECATED: HRMS database configuration has been migrated to OpenFeign-based API integration.
 * 
 * This file is kept for reference only and is no longer active.
 * All HRMS data access now goes through Feign clients in the `client` package.
 * 
 * @deprecated Use OpenFeign clients instead of direct database access.
 * @see com.ceodashboard.backend.hrms.client
 */
@Deprecated
@Configuration
public class HrmsDbConfig {

    private static final Logger logger = LoggerFactory.getLogger(HrmsDbConfig.class);

    public HrmsDbConfig() {
        logger.warn("HrmsDbConfig is deprecated and no longer active. Using OpenFeign clients instead.");
    }
}
