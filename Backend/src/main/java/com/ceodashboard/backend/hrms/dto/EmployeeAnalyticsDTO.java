package com.ceodashboard.backend.hrms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeAnalyticsDTO {
    private Long employeeId;
    private Double attendancePercentage;
    private long presentDays;
    private long absentDays;
    private Double leaveBalance;
    private Double performanceScore;
    private Double productivityScore;
}
