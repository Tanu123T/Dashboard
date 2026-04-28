package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSprintStatsDTO {
    private Long sprintId;
    private String sprintName;
    private String sprintStatus;
    private Integer storyPoints;
    private Integer bugsFixed;
    private Double hours;
    private Integer taskCompletionPercentage;
}
