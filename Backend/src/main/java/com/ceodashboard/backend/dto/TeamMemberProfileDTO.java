package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberProfileDTO {
    private Long id;
    private String name;
    private String role;
    private String projectName;
    private Integer totalStories;
    private Integer bugsResolved;
    private Double hoursWorked;
    private List<MemberSprintStatsDTO> sprintStats;
}
