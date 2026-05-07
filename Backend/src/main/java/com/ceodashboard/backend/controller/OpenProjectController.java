package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.repository.SprintRepository;
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

    @Autowired
    private SprintRepository sprintRepository;

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
        System.out.println("\n\n===== STARTING FULL SYNC =====");
        // Sync projects first
        System.out.println("[1/2] Syncing projects from OpenProject...");
        service.syncProjects();
        System.out.println("[1/2] Project sync complete\n");
        
        // Then sync sprints for each project
        List<Project> projects = projectRepository.findAll();
        System.out.println("[2/2] Found " + projects.size() + " projects in database. Starting sprint sync...");
        
        int successCount = 0;
        for (Project project : projects) {
            System.out.println("     → Syncing sprints for project ID " + project.getId() + " (" + project.getName() + ")");
            try {
                service.syncSprints(project.getId());
                successCount++;
                System.out.println("     ✓ Sprint sync completed for project " + project.getId());
            } catch (Exception ex) {
                System.out.println("     ✗ Sprint sync FAILED for project " + project.getId() + ": " + ex.getMessage());
            }
        }
        
        System.out.println("\n[2/2] Sprint sync complete. Successfully synced " + successCount + " out of " + projects.size() + " projects.");
        System.out.println("===== SYNC FINISHED =====\n");
        return "Projects and Sprints Synced! Synced " + successCount + " out of " + projects.size() + " projects.";
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
    /**
     * GET /sync/diagnose
     * Show diagnostic info about what's in the DB vs what's in OpenProject.
     */
    @GetMapping("/diagnose")
    public String diagnose() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n===== DIAGNOSTIC REPORT =====");
        sb.append("\n\n1. PROJECTS IN DATABASE:");
        
        List<Project> projects = projectRepository.findAll();
        if (projects.isEmpty()) {
            sb.append("\n   No projects found in database!");
        } else {
            for (Project p : projects) {
                sb.append("\n   - Project ").append(p.getId()).append(": ").append(p.getName());
            }
        }
        
        sb.append("\n\n2. SPRINTS IN DATABASE (by project_id):");
        for (Project p : projects) {
            long sprintCount = sprintCountByProject(p.getId());
            sb.append("\n   - Project ").append(p.getId()).append(": ").append(sprintCount).append(" sprints");
        }
        
        sb.append("\n\n3. API TEST - Fetch first project's versions:");
        if (!projects.isEmpty()) {
            Long projectId = projects.get(0).getId();
            try {
                Map<String, Object> versionsResponse = service.fetchResource("/projects/" + projectId + "/versions");
                Object total = versionsResponse.get("total");
                sb.append("\n   Project ").append(projectId).append(" has ").append(total).append(" versions in OpenProject");
            } catch (Exception ex) {
                sb.append("\n   ERROR fetching versions for project ").append(projectId).append(": ").append(ex.getMessage());
            }
        }
        
        sb.append("\n\n===== END DIAGNOSTIC REPORT =====\n");
        System.out.println(sb.toString());
        return sb.toString();
    }

    private long sprintCountByProject(Long projectId) {
        return sprintRepository.countByProjectId(projectId);
    }}