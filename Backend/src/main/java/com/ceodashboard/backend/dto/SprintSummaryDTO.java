package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSummaryDTO {
    private Integer totalPlannedSprints;
    private Integer completedSprints;
    private Integer activeSprints;
    private Double averageCompletion;
    private Integer teamSize;
}
