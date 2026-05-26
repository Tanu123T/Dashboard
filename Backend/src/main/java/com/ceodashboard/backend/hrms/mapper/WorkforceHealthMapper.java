package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class WorkforceHealthMapper {

    public WorkforceHealthTrendResponse toTrendResponse(Map<String, Object> queryResult) {
        return WorkforceHealthTrendResponse.builder()
                .month(queryResult.get("month").toString())
                .headcount(((Number) queryResult.get("headcount")).longValue())
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
