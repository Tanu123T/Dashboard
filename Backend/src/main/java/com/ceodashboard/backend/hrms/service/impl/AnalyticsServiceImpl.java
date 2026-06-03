package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.dto.EmployeeAnalyticsDTO;
import com.ceodashboard.backend.hrms.service.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Analytics service — all queries filtered by company_id from hrms.company.id.
 */
@Service
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JdbcTemplate jdbc;

    @Value("${hrms.company.id:1}")
    private long companyId;

    public AnalyticsServiceImpl(@Qualifier("hrmsJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Cacheable(value = "employeeAnalytics", key = "#employeeId")
    public EmployeeAnalyticsDTO getEmployeeAnalytics(Long employeeId) {
        log.info("Calculating analytics for employeeId={} companyId={}", employeeId, companyId);

        long presentDays = safeCount(
            "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND company_id = ? AND status = 'PRESENT'",
            employeeId, companyId);

        long absentDays = safeCount(
            "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND company_id = ? AND status = 'ABSENT'",
            employeeId, companyId);

        double attendancePercentage = 0.0;
        long total = presentDays + absentDays;
        if (total > 0) attendancePercentage = Math.round((presentDays * 100.0 / total) * 100.0) / 100.0;

        double leaveBalance = safeDouble(
            "SELECT COALESCE(balance, 0) FROM employee_leave_account WHERE employee_id = ? AND company_id = ? ORDER BY id DESC LIMIT 1",
            employeeId, companyId);

        double performanceScore = safeDouble(
            "SELECT COALESCE(AVG(CAST(ae.scored_points AS DECIMAL(10,2))), 0) FROM appraisal_evaluation ae WHERE ae.employee_id = ? AND ae.company_id = ?",
            employeeId, companyId);

        double productivityScore = safeDouble(
            "SELECT ROUND(COUNT(CASE WHEN status='PRESENT' THEN 1 END)*100.0/NULLIF(COUNT(*),0),2) " +
            "FROM attendance WHERE employee_id = ? AND company_id = ? AND DATE(date) >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)",
            employeeId, companyId);

        return EmployeeAnalyticsDTO.builder()
                .employeeId(employeeId)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(attendancePercentage)
                .performanceScore(performanceScore)
                .productivityScore(productivityScore)
                .leaveBalance(leaveBalance)
                .build();
    }

    private long safeCount(String sql, Object... args) {
        try {
            Long r = jdbc.queryForObject(sql, Long.class, args);
            return r != null ? r : 0L;
        } catch (DataAccessException ex) {
            log.warn("safeCount failed: {}", ex.getMessage());
            return 0L;
        }
    }

    private double safeDouble(String sql, Object... args) {
        try {
            Double r = jdbc.queryForObject(sql, Double.class, args);
            return r != null ? r : 0.0;
        } catch (DataAccessException ex) {
            log.warn("safeDouble failed: {}", ex.getMessage());
            return 0.0;
        }
    }
}
