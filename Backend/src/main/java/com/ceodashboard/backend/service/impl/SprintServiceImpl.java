package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.dto.*;
import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.entity.Sprint;
import com.ceodashboard.backend.entity.SprintTask;
import com.ceodashboard.backend.entity.TeamMember;
import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.repository.SprintRepository;
import com.ceodashboard.backend.repository.SprintTaskRepository;
import com.ceodashboard.backend.repository.TeamMemberRepository;
import com.ceodashboard.backend.service.SprintService;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SprintServiceImpl implements SprintService {

    private static final Integer DEFAULT_ORG_ID = 1;

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final TeamMemberRepository teamMemberRepository;

    public SprintServiceImpl(ProjectRepository projectRepository,
                             SprintRepository sprintRepository,
                             SprintTaskRepository sprintTaskRepository,
                             TeamMemberRepository teamMemberRepository) {
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public SprintDashboardDTO getSprintDashboard(Long projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectIdAndOrgIdOrderByStartDateDesc(projectId, DEFAULT_ORG_ID);
        
        SprintSummaryDTO summary = buildSprintSummary(projectId, sprints);
        List<TeamMemberDTO> team = buildTeamList(projectId);
        List<SprintListItemDTO> sprintItems = sprints.stream()
                .map(this::mapToListItemDTO)
                .collect(Collectors.toList());

        return SprintDashboardDTO.builder()
                .summary(summary)
                .team(team)
                .sprints(sprintItems)
                .build();
    }

    @Override
    public SprintDTO getSprintDetail(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .filter(s -> DEFAULT_ORG_ID.equals(s.getOrgId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        List<SprintTask> tasks = sprintTaskRepository.findBySprintId(sprintId);
        List<TeamMember> members = teamMemberRepository.findByProjectIdAndOrgId(sprint.getProjectId(), DEFAULT_ORG_ID);

        return mapToSprintDTO(sprint, tasks, members);
    }

    @Override
    public List<SprintDTO> getSprintsByProject(Long projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectIdAndOrgIdOrderByStartDateDesc(projectId, DEFAULT_ORG_ID);

        return sprints.stream().map(sprint -> {
            List<SprintTask> tasks = sprintTaskRepository.findBySprintId(sprint.getId());
            List<TeamMember> members = teamMemberRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID);
            return mapToSprintDTO(sprint, tasks, members);
        }).collect(Collectors.toList());
    }

    @Override
    public TeamMemberProfileDTO getTeamMemberProfile(Long projectId, String memberName) {
        List<Sprint> sprints = sprintRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID);

        int totalStories = 0;
        int bugsResolved = 0;
        double hoursWorked = 0;
        List<MemberSprintStatsDTO> sprintStats = new ArrayList<>();
        String role = "Team Member";

        for (Sprint sprint : sprints) {
            List<SprintTask> memberTasks = sprintTaskRepository.findBySprintIdAndAssignee(sprint.getId(), memberName);

            if (!memberTasks.isEmpty()) {
                int storyPoints = memberTasks.stream()
                        .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                        .sum();

                int bugs = (int) memberTasks.stream()
                        .filter(t -> "Bug".equalsIgnoreCase(t.getType()) &&
                                ("Closed".equalsIgnoreCase(t.getStatus()) || "Completed".equalsIgnoreCase(t.getStatus())))
                        .count();

                double hours = memberTasks.stream()
                        .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0)
                        .sum();

                long completed = memberTasks.stream()
                        .filter(t -> "Closed".equalsIgnoreCase(t.getStatus()) || "Completed".equalsIgnoreCase(t.getStatus()))
                        .count();

                int completionPercentage = memberTasks.isEmpty() ? 0 : (int) ((completed * 100) / memberTasks.size());

                totalStories += storyPoints;
                bugsResolved += bugs;
                hoursWorked += hours;

                TeamMember member = findTeamMemberByName(projectId, memberName);
                if (member != null && member.getRole() != null) {
                    role = member.getRole();
                }

                sprintStats.add(MemberSprintStatsDTO.builder()
                        .sprintId(sprint.getId())
                        .sprintName(sprint.getName())
                        .sprintStatus(sprint.getStatus())
                        .storyPoints(storyPoints)
                        .bugsFixed(bugs)
                        .hours(hours)
                        .taskCompletionPercentage(completionPercentage)
                        .build());
            }
        }

        return TeamMemberProfileDTO.builder()
                .id((long) memberName.hashCode())
                .name(memberName)
                .role(role)
                .projectName(resolveProjectName(projectId))
                .totalStories(totalStories)
                .bugsResolved(bugsResolved)
                .hoursWorked(hoursWorked)
                .sprintStats(sprintStats)
                .build();
    }

    @Override
    public List<SprintDTO> getAllSprints() {
        List<Sprint> sprints = sprintRepository.findByOrgId(DEFAULT_ORG_ID);

        sprints.sort(Comparator.comparing(Sprint::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        return sprints.stream().map(sprint -> {
            List<SprintTask> tasks = sprintTaskRepository.findBySprintId(sprint.getId());
            List<TeamMember> members = teamMemberRepository.findByProjectIdAndOrgId(sprint.getProjectId(), DEFAULT_ORG_ID);
            return mapToSprintDTO(sprint, tasks, members);
        }).collect(Collectors.toList());
    }

    private SprintSummaryDTO buildSprintSummary(Long projectId, List<Sprint> sprints) {
        int total = sprints.size();
        int completed = (int) sprints.stream().filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus())).count();
        int active = (int) sprints.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count();

        double avgCompletion = sprints.stream()
                .mapToDouble(s -> s.getProgress() != null ? s.getProgress() : 0.0)
                .average()
                .orElse(0.0);

        int teamSize = teamMemberRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID).size();

        return SprintSummaryDTO.builder()
                .totalPlannedSprints(total)
                .completedSprints(completed)
                .activeSprints(active)
                .averageCompletion(Math.round(avgCompletion * 10.0) / 10.0)
                .teamSize(Math.max(teamSize, 1))
                .build();
    }

    private List<TeamMemberDTO> buildTeamList(Long projectId) {
        return teamMemberRepository.findByProjectIdAndOrgId(projectId, DEFAULT_ORG_ID).stream()
                .filter(Objects::nonNull)
                .map(m -> TeamMemberDTO.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .role(m.getRole())
                        .avatar(m.getName() != null ? m.getName().substring(0, Math.min(2, m.getName().length())).toUpperCase() : "")
                        .build())
                .collect(Collectors.toList());
    }

    private SprintListItemDTO mapToListItemDTO(Sprint sprint) {
        String taskSummary = String.format("%s/%s", 
                sprint.getCompletedTasks() != null ? sprint.getCompletedTasks() : 0,
                sprint.getTotalTasks() != null ? sprint.getTotalTasks() : 0);

        return SprintListItemDTO.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .projectName(resolveProjectName(sprint.getProjectId()))
                .status(sprint.getStatus())
                .progress(sprint.getProgress() != null ? Math.round(sprint.getProgress()) : 0)
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .taskSummary(taskSummary)
                .build();
    }

    private TeamMember findTeamMemberByName(Long projectId, String memberName) {
        if (memberName == null || memberName.isBlank()) {
            return null;
        }

        String[] parts = memberName.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";

        return teamMemberRepository.findByProjectIdAndMemberFirstNameAndMemberLastNameAndOrgId(
                        projectId, firstName, lastName, DEFAULT_ORG_ID)
                .orElse(null);
    }

    private SprintDTO mapToSprintDTO(Sprint sprint, List<SprintTask> tasks, List<TeamMember> members) {
        int daysRemaining = calculateDaysRemaining(sprint.getEndDate());
        double timeUsedPercentage = calculateTimeUsedPercentage(sprint.getEstimatedHours(), sprint.getActualHours());

        List<SprintTaskDTO> taskDTOs = tasks.stream()
                .map(this::mapToTaskDTO)
                .collect(Collectors.toList());

        List<MemberWorkDTO> memberWorkDTOs = members.stream()
                .map(this::mapToMemberWorkDTO)
                .collect(Collectors.toList());

        List<BurndownDataDTO> burndownData = generateBurndownData(sprint, tasks);

        return SprintDTO.builder()
                .id(sprint.getId())
                .projectId(sprint.getProjectId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .scrumMaster(sprint.getScrumMaster())
                .projectName(resolveProjectName(sprint.getProjectId()))
                .progress(sprint.getProgress() != null ? Math.round(sprint.getProgress()) : 0)
                .daysRemaining(daysRemaining)
                .totalTasks(sprint.getTotalTasks())
                .completedTasks(sprint.getCompletedTasks())
                .inProgressTasks(sprint.getInProgressTasks())
                .todoTasks(sprint.getTodoTasks())
                .testingTasks(sprint.getTestingTasks())
                .storyPoints(sprint.getStoryPoints())
                .bugsFixed(sprint.getBugsFixed())
                .estimatedHours(sprint.getEstimatedHours())
                .actualHours(sprint.getActualHours())
                .timeUsedPercentage(timeUsedPercentage)
                .tasks(taskDTOs)
                .memberWork(memberWorkDTOs)
                .burndownChart(burndownData)
                .build();
    }

        private String resolveProjectName(Long projectId) {
                if (projectId == null) {
                        return "Unknown Project";
                }
                return projectRepository.findById(projectId)
                                .map(Project::getName)
                                .orElse("Project " + projectId);
        }

    private SprintTaskDTO mapToTaskDTO(SprintTask task) {
        return SprintTaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .type(task.getType())
                .storyPoints(task.getStoryPoints())
                .assignee(task.getAssignee())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .progressPercentage(task.getProgressPercentage())
                .build();
    }

    private MemberWorkDTO mapToMemberWorkDTO(TeamMember member) {
        return MemberWorkDTO.builder()
                .name(member.getName())
                .role(member.getRole())
                .totalTasks(null)
                .completedTasks(null)
                .inProgressTasks(null)
                .todoTasks(null)
                .donePercentage(0.0)
                .inProgressPercentage(0.0)
                .todoPercentage(0.0)
                .estimatedHours(null)
                .actualHours(null)
                .build();
    }

    private int calculateDaysRemaining(LocalDate endDate) {
        if (endDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return Math.max(0, (int) days);
    }

    private double calculateTimeUsedPercentage(Double estimated, Double actual) {
        if (estimated == null || estimated == 0) return 0.0;
        double percentage = (actual != null ? actual : 0) * 100.0 / estimated;
        return Math.round(percentage * 10.0) / 10.0;
    }

    private List<BurndownDataDTO> generateBurndownData(Sprint sprint, List<SprintTask> tasks) {
        List<BurndownDataDTO> data = new ArrayList<>();
        
        if (sprint.getStartDate() == null || sprint.getEndDate() == null) {
            return data;
        }

        long totalDays = ChronoUnit.DAYS.between(sprint.getStartDate(), sprint.getEndDate()) + 1;
        int totalStoryPoints = tasks.stream()
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();

        for (int day = 0; day < totalDays; day++) {
            String dayLabel = "D" + (day + 1);
            double ideal = totalStoryPoints * (1.0 - ((double) day / totalDays));
            
            // Calculate actual remaining based on completed tasks up to this day
            // In real scenario, this would come from time entries/history
            double actual = calculateActualRemaining(sprint.getStartDate().plusDays(day), tasks, totalStoryPoints);
            
            data.add(BurndownDataDTO.builder()
                    .day(dayLabel)
                    .ideal(Math.round(ideal * 10.0) / 10.0)
                    .actual(Math.round(actual * 10.0) / 10.0)
                    .build());
        }

        return data;
    }

    private double calculateActualRemaining(LocalDate date, List<SprintTask> tasks, int totalStoryPoints) {
        // Simplified: assume tasks completed reduce the remaining points
        // In real scenario, fetch from time entries or task history
        int completedPoints = tasks.stream()
                .filter(t -> t.getProgressPercentage() != null && t.getProgressPercentage() == 100)
                .mapToInt(t -> t.getStoryPoints() != null ? t.getStoryPoints() : 0)
                .sum();
        
        double remaining = totalStoryPoints - completedPoints;
        return Math.max(0, remaining);
    }
}
