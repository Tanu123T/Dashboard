package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeAnalyticsDTO;
import com.ceodashboard.backend.hrms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hrms/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('CEO', 'ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeAnalyticsDTO>> getEmployeeAnalytics(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getEmployeeAnalytics(employeeId)));
    }
}
