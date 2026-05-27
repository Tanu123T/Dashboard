package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.EmployeeAnalyticsDTO;

public interface AnalyticsService {
    EmployeeAnalyticsDTO getEmployeeAnalytics(Long employeeId);
}
