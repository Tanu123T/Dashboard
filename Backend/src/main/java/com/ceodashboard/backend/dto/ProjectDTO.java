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
public class ProjectDTO {
    private Long id;
    private String name;
    private String client;
    private String status;
    private Integer progress;
    private String lead;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String description;
    private List<String> techStack;
    private List<String> team;
    private Integer totalPlannedSprints;
    private Integer completedSprints;
    private Integer activeSprints;
    private List<SprintDTO> sprintTimeline;
}
