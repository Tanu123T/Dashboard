package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Workforce Health Summary Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceHealthSummaryResponse {

    private Long presentToday;
    private Long onBreak;
    private Long onLeave;
    private Long lateArrivals;
    private Long presentInOffice;
    private Double attendanceConsistency;
}
