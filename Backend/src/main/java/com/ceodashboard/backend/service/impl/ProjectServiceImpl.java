package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.dto.ProjectDTO;
import com.ceodashboard.backend.dto.ProjectSummaryDTO;
import com.ceodashboard.backend.dto.ProjectsPageResponseDTO;
import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.service.ProjectService;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectsPageResponseDTO getProjectsPageData() {
        List<Project> projects = projectRepository.findAllByOrderByNameAsc();
        
        ProjectSummaryDTO summary = ProjectSummaryDTO.builder()
                .totalProjects(projectRepository.count())
                .complete(projectRepository.countByStatus("Finished"))
                .inProgress(projectRepository.countByStatus("On track") + projectRepository.countByStatus("Not started"))
                .delayed(projectRepository.countByStatus("At risk") + projectRepository.countByStatus("Off track"))
                .build();

        List<ProjectDTO> projectItems = new ArrayList<>();
        for (Project p : projects) {
            projectItems.add(mapToListDTO(p));
        }

        return ProjectsPageResponseDTO.builder()
                .summary(summary)
                .projects(projectItems)
                .build();
    }

    @Override
    public ProjectDTO getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return mapToDetailDTO(project);
    }

    // For list view - includes essential fields for projects page
    private ProjectDTO mapToListDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .client(p.getClientName())
                .status(p.getStatus())
                .progress(p.getProgress())
                .lead(p.getLead())
                .dueDate(p.getDueDate())
                .description(p.getDescription())
                .team(splitCsv(p.getTeamCsv()))
                .totalPlannedSprints(p.getTotalPlannedSprints())
                .completedSprints(p.getCompletedSprints())
                .activeSprints(p.getActiveSprints())
                // Null fields not needed in list: techStack, sprintTimeline
                .build();
    }

    // For detail view - includes all fields
    private ProjectDTO mapToDetailDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .client(p.getClientName())
                .status(p.getStatus())
                .progress(p.getProgress())
                .lead(p.getLead())
                .startDate(p.getStartDate())
                .dueDate(p.getDueDate())
                .description(p.getDescription())
                .techStack(splitCsv(p.getTechStackCsv()))
                .team(splitCsv(p.getTeamCsv()))
                .totalPlannedSprints(p.getTotalPlannedSprints())
                .completedSprints(p.getCompletedSprints())
                .activeSprints(p.getActiveSprints())
                .sprintTimeline(buildSprintTimeline(p.getSprintNamesCsv(), p.getSprintStatesCsv()))
                .build();
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        String[] raw = csv.split(",");
        List<String> values = new ArrayList<>();
        for (String value : raw) {
            String cleaned = value.trim();
            if (!cleaned.isEmpty()) {
                values.add(cleaned);
            }
        }
        return values;
    }

    private List<com.ceodashboard.backend.dto.SprintDTO> buildSprintTimeline(String sprintNamesCsv, String sprintStatesCsv) {
        List<String> names = splitCsv(sprintNamesCsv);
        List<String> states = splitCsv(sprintStatesCsv);

        List<com.ceodashboard.backend.dto.SprintDTO> timeline = new ArrayList<>();
        int size = Math.min(names.size(), states.size());
        for (int i = 0; i < size; i++) {
            timeline.add(com.ceodashboard.backend.dto.SprintDTO.builder()
                    .name(names.get(i))
                    .status(states.get(i))
                    .build());
        }

        return timeline;
    }
}
