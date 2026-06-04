package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAnalyticsDTO {
    private Integer totalDays;
    private Integer presentDays;
    private Integer absentDays;
    private Double  attendancePercentage;
}
