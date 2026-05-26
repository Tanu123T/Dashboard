package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Workforce Health Watchlist Response DTO
 * 
 * Contains alerts for employees with attendance issues
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceHealthWatchlistResponse {

    private Long attendanceId;

    /** Attendance status */
    private String status;

    /** Attendance date */
    private LocalDate attendanceDate;

    /** Risk level */
    private String riskLevel;
}
