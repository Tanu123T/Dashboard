package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintTaskDTO {
    private Long id;
    private String title;
    private String status;
    private String type;
    private Integer storyPoints;
    private String assignee;
    private Double estimatedHours;
    private Double actualHours;
    private Integer progressPercentage;
}
