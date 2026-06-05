package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.client.AttendanceClient;
import com.ceodashboard.backend.hrms.client.EmployeeLeaveAccountClient;
import com.ceodashboard.backend.hrms.dto.AttendanceDTO;
import com.ceodashboard.backend.hrms.dto.EmployeeAnalyticsDTO;
import com.ceodashboard.backend.hrms.dto.EmployeeLeaveAccountDTO;
import com.ceodashboard.backend.hrms.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Analytics service implemented with Feign clients for HRMS API integration.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AttendanceClient attendanceClient;
    private final EmployeeLeaveAccountClient leaveAccountClient;

    @Override
    @Cacheable(value = "employeeAnalytics", key = "#employeeId")
    public EmployeeAnalyticsDTO getEmployeeAnalytics(Long employeeId) {
        log.info("Calculating analytics for employeeId={}", employeeId);

        try {
            String empIdStr = employeeId.toString();
            LocalDate now = LocalDate.now();
            LocalDate windowStart = now.minusDays(30);
            LocalDate productivityWindowStart = now.minusDays(90);

            // Fetch attendance data from HRMS API
            List<AttendanceDTO> attendanceRecords = attendanceClient.getAttendanceByEmployeeId(empIdStr);

            // Calculate attendance metrics for last 30 days
            long presentDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    a.getDate().isAfter(windowStart) &&
                    a.getDate().isBefore(now) &&
                    "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();

            long absentDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    a.getDate().isAfter(windowStart) &&
                    a.getDate().isBefore(now) &&
                    "ABSENT".equalsIgnoreCase(a.getStatus()))
                .count();

            double attendancePercentage = 0.0;
            long totalAttendanceDays = presentDays + absentDays;
            if (totalAttendanceDays > 0) {
                attendancePercentage = Math.round((presentDays * 100.0 / totalAttendanceDays) * 100.0) / 100.0;
            }

            // Fetch leave balance
            List<EmployeeLeaveAccountDTO> leaveAccounts = leaveAccountClient.getLeaveAccountsByEmployeeId(empIdStr);
            double leaveBalance = leaveAccounts.stream()
                .mapToDouble(account -> account.getBalance() != null ? account.getBalance() : 0.0)
                .sum();

            // Calculate productivity score for last 90 days
            long productivityScore = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    a.getDate().isAfter(productivityWindowStart) &&
                    a.getDate().isBefore(now) &&
                    "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();

            long totalWindowDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    a.getDate().isAfter(productivityWindowStart) &&
                    a.getDate().isBefore(now))
                .count();

            double productivityPercentage = totalWindowDays == 0 ? 0.0 :
                Math.round((productivityScore * 100.0 / totalWindowDays) * 100.0) / 100.0;

            return EmployeeAnalyticsDTO.builder()
                .employeeId(employeeId)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(attendancePercentage)
                .performanceScore(0.0) // Performance score would come from appraisal API
                .productivityScore(productivityPercentage)
                .leaveBalance(leaveBalance)
                .build();
        } catch (Exception e) {
            log.error("Error calculating analytics for employeeId: {}", employeeId, e);
            return EmployeeAnalyticsDTO.builder()
                .employeeId(employeeId)
                .presentDays(0)
                .absentDays(0)
                .attendancePercentage(0.0)
                .performanceScore(0.0)
                .productivityScore(0.0)
                .leaveBalance(0.0)
                .build();
        }
    }
}
