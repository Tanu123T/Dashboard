package com.ceodashboard.backend.controller;

import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProjectsPage() {
        List<Project> projects = projectRepository.findAllByOrderByNameAsc();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("summary", buildSummary());

        List<Map<String, Object>> items = new ArrayList<>();
        for (Project p : projects) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("client", p.getClientName());
            item.put("status", p.getStatus());
            item.put("progress", p.getProgress());
            item.put("lead", p.getLead());
            item.put("startDate", p.getStartDate());
            item.put("dueDate", p.getDueDate());
            item.put("totalPlannedSprints", p.getTotalPlannedSprints());
            item.put("completedSprints", p.getCompletedSprints());
            item.put("activeSprints", p.getActiveSprints());
            items.add(item);
        }

        response.put("projects", items);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectDetail(@PathVariable Long id) {
        Project p = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", p.getId());
        response.put("name", p.getName());
        response.put("client", p.getClientName());
        response.put("status", p.getStatus());
        response.put("progress", p.getProgress());
        response.put("lead", p.getLead());
        response.put("startDate", p.getStartDate());
        response.put("dueDate", p.getDueDate());
        response.put("description", p.getDescription());
        response.put("techStack", splitCsv(p.getTechStackCsv()));
        response.put("team", splitCsv(p.getTeamCsv()));
        response.put("totalPlannedSprints", p.getTotalPlannedSprints());
        response.put("completedSprints", p.getCompletedSprints());
        response.put("activeSprints", p.getActiveSprints());
        response.put("sprintTimeline", buildSprintTimeline(p.getSprintNamesCsv(), p.getSprintStatesCsv()));

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProjects", projectRepository.count());
        summary.put("complete", projectRepository.countByStatus("COMPLETE"));
        summary.put("inProgress", projectRepository.countByStatus("IN_PROGRESS"));
        summary.put("delayed", projectRepository.countByStatus("DELAYED"));
        return summary;
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

    private List<Map<String, String>> buildSprintTimeline(String sprintNamesCsv, String sprintStatesCsv) {
        List<String> names = splitCsv(sprintNamesCsv);
        List<String> states = splitCsv(sprintStatesCsv);

        List<Map<String, String>> timeline = new ArrayList<>();
        int size = Math.min(names.size(), states.size());
        for (int i = 0; i < size; i++) {
            Map<String, String> sprint = new LinkedHashMap<>();
            sprint.put("name", names.get(i));
            sprint.put("state", states.get(i));
            timeline.add(sprint);
        }

        return timeline;
    }
}