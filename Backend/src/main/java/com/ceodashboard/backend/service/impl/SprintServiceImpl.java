package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.dto.*;
import com.ceodashboard.backend.entity.Sprint;
import com.ceodashboard.backend.entity.SprintTask;
import com.ceodashboard.backend.entity.TeamMember;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final SprintTaskRepository sprintTaskRepository;
    private final TeamMemberRepository teamMemberRepository;

    public SprintServiceImpl(SprintRepository sprintRepository, 
                             SprintTaskRepository sprintTaskRepository,
                             TeamMemberRepository teamMemberRepository) {
        this.sprintRepository = sprintRepository;
        this.sprintTaskRepository = sprintTaskRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public SprintDashboardDTO getSprintDashboard(Long projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateDesc(projectId);
        
        SprintSummaryDTO summary = buildSprintSummary(projectId, sprints);
        List<TeamMemberDTO> team = buildTeamList(projectId, sprints);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));

        List<SprintTask> tasks = sprintTaskRepository.findBySprintId(sprintId);
        List<TeamMember> members = teamMemberRepository.findBySprintId(sprintId);

        return mapToSprintDTO(sprint, tasks, members);
    }

    @Override
    public List<SprintDTO> getSprintsByProject(Long projectId) {
        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByStartDateDesc(projectId);
        
        return sprints.stream().map(sprint -> {
            List<SprintTask> tasks = sprintTaskRepository.findBySprintId(sprint.getId());
            List<TeamMember> members = teamMemberRepository.findBySprintId(sprint.getId());
            return mapToSprintDTO(sprint, tasks, members);
        }).collect(Collectors.toList());
    }

    @Override
    public TeamMemberProfileDTO getTeamMemberProfile(Long projectId, String memberName) {
        List<Sprint> sprints = sprintRepository.findByProjectId(projectId);
        
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

                // Get role from first task's assignee pattern or member record
                TeamMember member = teamMemberRepository.findBySprintIdAndName(sprint.getId(), memberName)
                        .orElse(null);
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
                .projectName("Data Analytics Engine") // Can be fetched from project repo
                .totalStories(totalStories)
                .bugsResolved(bugsResolved)
                .hoursWorked(hoursWorked)
                .sprintStats(sprintStats)
                .build();
    }

    private SprintSummaryDTO buildSprintSummary(Long projectId, List<Sprint> sprints) {
        int total = sprints.size();
        int completed = (int) sprints.stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();
        int active = (int) sprints.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();
        
        double avgCompletion = sprints.stream()
                .mapToInt(Sprint::getProgress)
                .average()
                .orElse(0.0);

        // Count unique team members across all sprints
        int teamSize = sprints.stream()
                .flatMap(s -> teamMemberRepository.findBySprintId(s.getId()).stream())
                .map(TeamMember::getName)
                .distinct()
                .mapToInt(name -> 1)
                .sum();

        return SprintSummaryDTO.builder()
                .totalPlannedSprints(total)
                .completedSprints(completed)
                .activeSprints(active)
                .averageCompletion(Math.round(avgCompletion * 10.0) / 10.0)
                .teamSize(Math.max(teamSize, 1))
                .build();
    }

    private List<TeamMemberDTO> buildTeamList(Long projectId, List<Sprint> sprints) {
        // Get unique team members from all sprints
        return sprints.stream()
                .flatMap(s -> teamMemberRepository.findBySprintId(s.getId()).stream())
                .collect(Collectors.toMap(
                        TeamMember::getName,
                        m -> TeamMemberDTO.builder()
                                .id(m.getId())
                                .name(m.getName())
                                .role(m.getRole())
                                .avatar(m.getName().substring(0, Math.min(2, m.getName().length())).toUpperCase())
                                .build(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }

    private SprintListItemDTO mapToListItemDTO(Sprint sprint) {
        String taskSummary = String.format("%d/%d", 
                sprint.getCompletedTasks() != null ? sprint.getCompletedTasks() : 0,
                sprint.getTotalTasks() != null ? sprint.getTotalTasks() : 0);

        return SprintListItemDTO.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .projectName("Data Analytics Engine") // Can be fetched from project
                .status(sprint.getStatus())
                .progress(sprint.getProgress())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .taskSummary(taskSummary)
                .build();
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
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .scrumMaster(sprint.getScrumMaster())
                .projectName("Data Analytics Engine")
                .progress(sprint.getProgress())
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
        int total = member.getAssignedTasks() != null ? member.getAssignedTasks() : 0;
        double donePct = total > 0 ? (member.getCompletedTasks() * 100.0 / total) : 0;
        double inProgressPct = total > 0 ? (member.getInProgressTasks() * 100.0 / total) : 0;
        double todoPct = total > 0 ? (member.getTodoTasks() * 100.0 / total) : 0;

        return MemberWorkDTO.builder()
                .name(member.getName())
                .role(member.getRole())
                .totalTasks(total)
                .completedTasks(member.getCompletedTasks())
                .inProgressTasks(member.getInProgressTasks())
                .todoTasks(member.getTodoTasks())
                .donePercentage(Math.round(donePct * 10.0) / 10.0)
                .inProgressPercentage(Math.round(inProgressPct * 10.0) / 10.0)
                .todoPercentage(Math.round(todoPct * 10.0) / 10.0)
                .estimatedHours(member.getEstimatedHours())
                .actualHours(member.getActualHours())
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
