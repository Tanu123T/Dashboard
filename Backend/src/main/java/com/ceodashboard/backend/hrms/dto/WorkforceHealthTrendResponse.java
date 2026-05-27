package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Workforce Health Headcount Trend Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceHealthTrendResponse {

    private String month;
    private Long headcount;
}
