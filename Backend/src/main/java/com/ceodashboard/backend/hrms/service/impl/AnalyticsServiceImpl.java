package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.dto.EmployeeAnalyticsDTO;
import com.ceodashboard.backend.hrms.repository.AttendanceRepository;
import com.ceodashboard.backend.hrms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "hrmsTransactionManager", readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AttendanceRepository attendanceRepository;

    @Override
    @Cacheable(value = "employeeAnalytics", key = "#employeeId")
    public EmployeeAnalyticsDTO getEmployeeAnalytics(Long employeeId) {
        log.info("Calculating analytics for employee id: {}", employeeId);
        
        long presentDays = attendanceRepository.countByEmployeeIdAndStatus(employeeId, "PRESENT");
        long absentDays = attendanceRepository.countByEmployeeIdAndStatus(employeeId, "ABSENT");
        
        double attendancePercentage = 0.0;
        if ((presentDays + absentDays) > 0) {
            attendancePercentage = (double) presentDays / (presentDays + absentDays) * 100;
        }

        return EmployeeAnalyticsDTO.builder()
                .employeeId(employeeId)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                .performanceScore(85.5) // Placeholder
                .productivityScore(90.0) // Placeholder
                .leaveBalance(12.0) // Placeholder
                .build();
    }
}
