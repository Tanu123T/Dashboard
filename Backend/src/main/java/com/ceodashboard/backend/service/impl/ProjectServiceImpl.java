package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.dto.ProjectDTO;
import com.ceodashboard.backend.dto.ProjectSummaryDTO;
import com.ceodashboard.backend.dto.ProjectsPageResponseDTO;
import com.ceodashboard.backend.dto.SprintDTO;
import com.ceodashboard.backend.entity.Organization;
import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.entity.ProjectSchedule;
import com.ceodashboard.backend.entity.Sprint;
import com.ceodashboard.backend.entity.TeamMember;
import com.ceodashboard.backend.repository.OrganizationRepository;
import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.repository.ProjectScheduleRepository;
import com.ceodashboard.backend.repository.SprintRepository;
import com.ceodashboard.backend.repository.TeamMemberRepository;
import com.ceodashboard.backend.service.ProjectService;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Integer DEFAULT_ORG_ID = 1;

    private final ProjectRepository projectRepository;
    private final ProjectScheduleRepository projectScheduleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SprintRepository sprintRepository;
    private final OrganizationRepository organizationRepository;

    public ProjectServiceImpl(
            ProjectRepository projectRepository,
            ProjectScheduleRepository projectScheduleRepository,
            TeamMemberRepository teamMemberRepository,
            SprintRepository sprintRepository,
            OrganizationRepository organizationRepository) {
        this.projectRepository = projectRepository;
        this.projectScheduleRepository = projectScheduleRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.sprintRepository = sprintRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public ProjectsPageResponseDTO getProjectsPageData() {
        List<Project> projects = projectRepository.findAllByOrgIdOrderByNameAsc(DEFAULT_ORG_ID);

        ProjectSummaryDTO summary = ProjectSummaryDTO.builder()
                .totalProjects(projectRepository.countByOrgId(DEFAULT_ORG_ID))
                .complete(projectRepository.countByOrgIdAndStatus(DEFAULT_ORG_ID, "Finished"))
                .inProgress(projectRepository.countByOrgIdAndStatus(DEFAULT_ORG_ID, "On track")
                        + projectRepository.countByOrgIdAndStatus(DEFAULT_ORG_ID, "Not started"))
                .delayed(projectRepository.countByOrgIdAndStatus(DEFAULT_ORG_ID, "At risk")
                        + projectRepository.countByOrgIdAndStatus(DEFAULT_ORG_ID, "Off track"))
                .build();

        List<ProjectDTO> projectItems = projects.stream()
                .map(this::mapToListDTO)
                .collect(Collectors.toList());

        return ProjectsPageResponseDTO.builder()
                .summary(summary)
                .projects(projectItems)
                .build();
    }

    @Override
    public ProjectDTO getProjectDetail(Long projectId) {
        Project project = projectRepository.findByIdAndOrgId(projectId, DEFAULT_ORG_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        return mapToDetailDTO(project);
    }

    private ProjectDTO mapToListDTO(Project p) {
        ProjectSchedule schedule = getSchedule(p.getId());

        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .client(resolveOrganizationName(p.getOrgId()))
                .status(p.getStatus())
                .progress(p.getProgress())
                .lead(p.getLead())
                .dueDate(schedule != null ? schedule.getProjectDeadline() : null)
                .description(p.getDescription())
                .techStack(Collections.emptyList())
                .team(getProjectTeamNames(p.getId()))
                .totalPlannedSprints(schedule != null ? schedule.getTotalNoOfSprints() : null)
                .completedSprints((int) sprintRepository.countByProjectIdAndOrgIdAndStatus(p.getId(), DEFAULT_ORG_ID, "COMPLETED"))
                .activeSprints((int) sprintRepository.countByProjectIdAndOrgIdAndStatus(p.getId(), DEFAULT_ORG_ID, "ACTIVE"))
                .build();
    }

    private ProjectDTO mapToDetailDTO(Project p) {
        ProjectSchedule schedule = getSchedule(p.getId());
        List<Sprint> projectSprints = sprintRepository.findByProjectIdAndOrgIdOrderByStartDateDesc(p.getId(), DEFAULT_ORG_ID);

        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .client(resolveOrganizationName(p.getOrgId()))
                .status(p.getStatus())
                .progress(p.getProgress())
                .lead(p.getLead())
                .startDate(schedule != null ? schedule.getProjectStartDate() : null)
                .dueDate(schedule != null ? schedule.getProjectDeadline() : null)
                .description(p.getDescription())
                .techStack(Collections.emptyList())
                .team(getProjectTeamNames(p.getId()))
                .totalPlannedSprints(schedule != null ? schedule.getTotalNoOfSprints() : null)
                .completedSprints((int) sprintRepository.countByProjectIdAndOrgIdAndStatus(p.getId(), DEFAULT_ORG_ID, "COMPLETED"))
                .activeSprints((int) sprintRepository.countByProjectIdAndOrgIdAndStatus(p.getId(), DEFAULT_ORG_ID, "ACTIVE"))
                .sprintTimeline(buildSprintTimeline(projectSprints))
                .build();
    }

    private ProjectSchedule getSchedule(Long projectId) {
        return projectScheduleRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID)
                .orElse(null);
    }

    private String resolveOrganizationName(Integer orgId) {
        if (orgId == null) {
            return "Unknown Organization";
        }
        return organizationRepository.findById(orgId)
                .map(Organization::getOrgName)
                .orElse("Organization " + orgId);
    }

    private List<String> getProjectTeamNames(Long projectId) {
        return teamMemberRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID).stream()
                .map(TeamMember::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<SprintDTO> buildSprintTimeline(List<Sprint> sprints) {
        return sprints.stream()
                .map(s -> SprintDTO.builder()
                        .name(s.getName())
                        .status(s.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
