package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.dto.ProjectDTO;
import com.ceodashboard.backend.dto.ProjectsPageResponseDTO;
import com.ceodashboard.backend.service.ProjectService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * GET /projects
     * Returns consolidated project page data including summary stats and all projects.
     * This single endpoint replaces multiple scattered endpoints.
     */
    @GetMapping
    public ResponseEntity<ProjectsPageResponseDTO> getProjectsPage() {
        return ResponseEntity.ok(projectService.getProjectsPageData());
    }

    /**
     * GET /projects/{id}
     * Returns detailed project information including:
     * - Basic info (name, client, status, dates)
     * - Progress and sprint statistics
     * - Team members and tech stack
     * - Sprint timeline
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectDetail(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectDetail(id));
    }
}