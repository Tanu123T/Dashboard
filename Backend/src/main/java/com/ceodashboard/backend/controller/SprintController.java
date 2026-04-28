package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.dto.SprintDTO;
import com.ceodashboard.backend.dto.SprintDashboardDTO;
import com.ceodashboard.backend.dto.TeamMemberProfileDTO;
import com.ceodashboard.backend.service.SprintService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    /**
     * GET /sprints/project/{projectId}
     * Get sprint dashboard for a project including summary, team, and all sprints.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<SprintDashboardDTO> getSprintDashboard(@PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.getSprintDashboard(projectId));
    }

    /**
     * GET /sprints/{sprintId}
     * Get detailed information for a single sprint including tasks, work distribution, and burndown.
     */
    @GetMapping("/{sprintId}")
    public ResponseEntity<SprintDTO> getSprintDetail(@PathVariable Long sprintId) {
        return ResponseEntity.ok(sprintService.getSprintDetail(sprintId));
    }

    /**
     * GET /sprints/project/{projectId}/all
     * Get all sprints for a project with full details.
     */
    @GetMapping("/project/{projectId}/all")
    public ResponseEntity<List<SprintDTO>> getSprintsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.getSprintsByProject(projectId));
    }

    /**
     * GET /sprints/project/{projectId}/member/{memberName}
     * Get team member performance profile across all sprints.
     */
    @GetMapping("/project/{projectId}/member/{memberName}")
    public ResponseEntity<TeamMemberProfileDTO> getTeamMemberProfile(
            @PathVariable Long projectId,
            @PathVariable String memberName) {
        return ResponseEntity.ok(sprintService.getTeamMemberProfile(projectId, memberName));
    }
}
