package com.ceodashboard.backend.hrms.mapper;

import com.ceodashboard.backend.hrms.dto.AttendanceAnalyticsDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AttendanceSummaryRowMapper implements RowMapper<AttendanceAnalyticsDTO> {

    @Override
    public AttendanceAnalyticsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return AttendanceAnalyticsDTO.builder()
                .totalDays(rs.getInt("total_days"))
                .presentDays(rs.getInt("present_days"))
                .absentDays(rs.getInt("absent_days"))
                .attendancePercentage(rs.getDouble("attendance_percentage"))
                .build();
    }
}
