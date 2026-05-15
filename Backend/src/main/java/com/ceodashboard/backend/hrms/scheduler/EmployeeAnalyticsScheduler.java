package com.ceodashboard.backend.hrms.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmployeeAnalyticsScheduler {

    /**
     * Clear all employee and analytics caches every hour to ensure data consistency
     * with the database.
     */
    @Scheduled(cron = "0 0 * * * *")
    @CacheEvict(value = {"employees", "employeeAnalytics"}, allEntries = true)
    public void clearHrmsCaches() {
        log.info("Cleared HRMS caches: employees, employeeAnalytics");
    }
}
