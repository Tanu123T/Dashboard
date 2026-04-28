package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintListItemDTO {
    private Long id;
    private String name;
    private String projectName;
    private String status;
    private Integer progress;
    private LocalDate startDate;
    private LocalDate endDate;
    private String taskSummary; // e.g., "3/7" for 3 done out of 7
}
