package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.Map;

@Component
public class WorkforceHealthMapper {

    /**
     * Helper method to parse LocalDate from various formats
     */
    private LocalDate parseLocalDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).toLocalDate();
        } else if (dateObj instanceof Date) {
            return ((Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate();
        } else if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj);
        }
        return null;
    }

    /**
     * Convert MySQL query result (Map) to Trend Response DTO
     */
    public WorkforceHealthTrendResponse toTrendResponse(Map<String, Object> queryResult) {
        // Handle column name case variations from MySQL
        String month = null;
        Long headcount = null;

        // Try different column name cases
        if (queryResult.containsKey("month")) {
            month = queryResult.get("month").toString();
        } else if (queryResult.containsKey("MONTH")) {
            month = queryResult.get("MONTH").toString();
        }

        // Get headcount value
        if (queryResult.containsKey("headcount")) {
            headcount = ((Number) queryResult.get("headcount")).longValue();
        } else if (queryResult.containsKey("HEADCOUNT")) {
            headcount = ((Number) queryResult.get("HEADCOUNT")).longValue();
        }

        return WorkforceHealthTrendResponse.builder()
                .month(month)
                .headcount(headcount)
                .build();
    }

    /**
     * Convert Attendance entity to Attendance Log Response DTO
     */
    public WorkforceHealthAttendanceLogResponse toAttendanceLogResponse(Attendance attendance) {
        Long employeeId = null;
        String department = "";

        if (attendance.getEmployee() != null) {
            employeeId = attendance.getEmployee().getId();
            if (attendance.getEmployee().getDepartment() != null) {
                department = attendance.getEmployee().getDepartment().getName() != null
                        ? attendance.getEmployee().getDepartment().getName()
                        : "";
            }
        }

        return WorkforceHealthAttendanceLogResponse.builder()
                .attendanceId(attendance.getId())
                .employeeId(employeeId)
                .department(department)
                .attendanceDate(attendance.getAttendanceDate())
                .hasCheckedIn(attendance.getHasCheckedIn())
                .status(attendance.getStatus())
                .workHours(attendance.getWorkHours())
                .checkOut("")
                .build();
    }

    /**
     * Convert Map from SQL query (with JOINs) directly to DTO - avoids entity
     * loading
     */
    public WorkforceHealthAttendanceLogResponse toAttendanceLogResponseFromMap(Map<String, Object> map) {
        Long attendanceId = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
        Long employeeId = map.get("employee_id") != null ? ((Number) map.get("employee_id")).longValue() : null;
        String department = map.get("dept_name") != null ? map.get("dept_name").toString() : "Unknown";
        LocalDate attendanceDate = parseLocalDate(map.get("attendanceDate"));
        Boolean hasCheckedIn = map.get("has_checked_in") != null ? (Boolean) map.getOrDefault("has_checked_in", false)
                : false;
        String status = map.get("status") != null ? map.get("status").toString() : "";
        String workHours = map.get("work_hours") != null ? map.get("work_hours").toString() : "0";
        String checkOut = map.get("checkout_time") != null ? map.get("checkout_time").toString() : "";

        return WorkforceHealthAttendanceLogResponse.builder()
                .attendanceId(attendanceId)
                .employeeId(employeeId)
                .department(department)
                .attendanceDate(attendanceDate)
                .hasCheckedIn(hasCheckedIn)
                .status(status)
                .workHours(workHours)
                .checkOut(checkOut)
                .build();
    }

    /**
     * Convert Attendance entity to Watchlist Response DTO
     */
    public WorkforceHealthWatchlistResponse toWatchlistResponse(Attendance attendance) {
        Long employeeId = null;
        String department = "";

        if (attendance.getEmployee() != null) {
            employeeId = attendance.getEmployee().getId();
            if (attendance.getEmployee().getDepartment() != null) {
                department = attendance.getEmployee().getDepartment().getName() != null
                        ? attendance.getEmployee().getDepartment().getName()
                        : "";
            }
        }

        return WorkforceHealthWatchlistResponse.builder()
                .attendanceId(attendance.getId())
                .employeeId(employeeId)
                .department(department)
                .status(attendance.getStatus())
                .attendanceDate(attendance.getAttendanceDate())
                .riskLevel("HIGH")
                .build();
    }

    /**
     * Convert Map from SQL query (with JOINs) directly to DTO - avoids entity
     * loading
     */
    public WorkforceHealthWatchlistResponse toWatchlistResponseFromMap(Map<String, Object> map) {
        Long attendanceId = map.get("id") != null ? ((Number) map.get("id")).longValue() : null;
        Long employeeId = map.get("employee_id") != null ? ((Number) map.get("employee_id")).longValue() : null;
        String department = map.get("dept_name") != null ? map.get("dept_name").toString() : "Unknown";
        String status = map.get("status") != null ? map.get("status").toString() : "";
        LocalDate attendanceDate = parseLocalDate(map.get("attendanceDate"));

        return WorkforceHealthWatchlistResponse.builder()
                .attendanceId(attendanceId)
                .employeeId(employeeId)
                .department(department)
                .status(status)
                .attendanceDate(attendanceDate)
                .riskLevel("HIGH")
                .build();
    }
}
