package com.ceodashboard.backend.service;

import com.ceodashboard.backend.dto.SprintDTO;
import com.ceodashboard.backend.dto.SprintDashboardDTO;
import com.ceodashboard.backend.dto.TeamMemberProfileDTO;

import java.util.List;

public interface SprintService {
    
    /**
     * Get sprint dashboard data for a project including summary, team, and sprint list.
     */
    SprintDashboardDTO getSprintDashboard(Long projectId);
    
    /**
     * Get detailed information for a single sprint including tasks, members, and burndown.
     */
    SprintDTO getSprintDetail(Long sprintId);
    
    /**
     * Get all sprints for a project.
     */
    List<SprintDTO> getSprintsByProject(Long projectId);
    
    /**
     * Get team member performance profile across sprints.
     */
    TeamMemberProfileDTO getTeamMemberProfile(Long projectId, String memberName);
}
