package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintDTO {
    private Long id;
    private String name;
    private String goal;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String scrumMaster;
    private String projectName;
    private Integer progress;
    private Integer daysRemaining;
    
    // Task stats
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer todoTasks;
    private Integer testingTasks;
    
    // Time tracking
    private Integer storyPoints;
    private Integer bugsFixed;
    private Double estimatedHours;
    private Double actualHours;
    private Double timeUsedPercentage;
    
    // Related data
    private List<SprintTaskDTO> tasks;
    private List<MemberWorkDTO> memberWork;
    private List<BurndownDataDTO> burndownChart;
}
