package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WorkforceHealthMapper {

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

    public WorkforceHealthAttendanceLogResponse toAttendanceLogResponse(Attendance attendance) {
        return WorkforceHealthAttendanceLogResponse.builder()
                .attendanceId(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .hasCheckedIn(attendance.getHasCheckedIn())
                .status(attendance.getStatus())
                .workHours(attendance.getWorkHours())
                .build();
    }

    public WorkforceHealthWatchlistResponse toWatchlistResponse(Attendance attendance) {
        return WorkforceHealthWatchlistResponse.builder()
                .attendanceId(attendance.getId())
                .status(attendance.getStatus())
                .attendanceDate(attendance.getAttendanceDate())
                .riskLevel("HIGH")
                .build();
    }
}
