package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.service.OpenProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class OpenProjectController {

    @Autowired
    private OpenProjectService service;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * GET /sync
     * Syncs all projects from OpenProject.
     */
    @GetMapping
    public String sync() {
        service.syncProjects();
        return "Projects Synced!";
    }

    /**
     * GET /sync/all
     * Syncs projects AND all sprints for each project.
     */
    @GetMapping("/all")
    public String syncAll() {
        // Sync projects first
        service.syncProjects();
        
        // Then sync sprints for each project
        List<Project> projects = projectRepository.findAll();
        System.out.println("DEBUG: Found " + projects.size() + " projects in database");
        
        for (Project project : projects) {
            System.out.println("DEBUG: Syncing sprints for project " + project.getId() + " - " + project.getName());
            service.syncSprints(project.getId());
        }
        
        return "Projects and Sprints Synced! Synced " + projects.size() + " projects.";
    }

    /**
     * GET /sync/sprints/{projectId}
     * Sync sprints for a specific project.
     */
    @GetMapping("/sprints/{projectId}")
    public String syncSprints(@PathVariable Long projectId) {
        service.syncSprints(projectId);
        return "Sprints synced for project " + projectId;
    }

    /**
     * GET /sync/raw?path=/projects
     * Fetch raw OpenProject API response for debugging.
     */
    @GetMapping("/raw")
    public ResponseEntity<Map<String, Object>> fetchRaw(@RequestParam String path) {
        return ResponseEntity.ok(service.fetchResource(path));
    }
}