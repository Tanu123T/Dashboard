package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.entity.Sprint;
import com.ceodashboard.backend.entity.SprintTask;
import com.ceodashboard.backend.entity.TeamMember;
import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.repository.SprintRepository;
import com.ceodashboard.backend.repository.SprintTaskRepository;
import com.ceodashboard.backend.repository.TeamMemberRepository;
import com.ceodashboard.backend.service.OpenProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.*;

@Service
public class OpenProjectServiceImpl implements OpenProjectService {

    private static final Logger log = LoggerFactory.getLogger(OpenProjectServiceImpl.class);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private SprintTaskRepository sprintTaskRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Value("${openproject.base-url:https://ceodashboarddemo.openproject.com/api/v3}")
    private String baseUrl;

    @Value("${openproject.auth-header:}")
    private String authHeader;

    private RestTemplate restTemplate = new RestTemplate();

    private String normalizeAuthHeader(String configuredHeader) {
        if (configuredHeader == null || configuredHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OPENPROJECT_AUTH_HEADER is not configured");
        }

        String header = configuredHeader.trim();
        if (header.startsWith("\"") && header.endsWith("\"") && header.length() > 1) {
            header = header.substring(1, header.length() - 1).trim();
        }

        // Convenience: accept raw OpenProject API key and convert it to Basic auth.
        if (header.startsWith("opapi-")) {
            String basicPayload = "apikey:" + header;
            String encoded = Base64.getEncoder().encodeToString(basicPayload.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encoded;
        }

        if (header.startsWith("Basic ")) {
            return header;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "OPENPROJECT_AUTH_HEADER must be either 'opapi-...' or 'Basic <base64(apikey:opapi-...)>'"
        );
    }

    @Override
    public Map<String, Object> fetchResource(String path) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String baseUrlNormalized = baseUrl != null ? baseUrl.trim() : "";
        String authHeaderNormalized = normalizeAuthHeader(authHeader);

        if (baseUrlNormalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OPENPROJECT_BASE_URL is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeaderNormalized);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    baseUrlNormalized + normalizedPath,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
        } catch (RestClientResponseException ex) {
            int upstreamStatus = ex.getStatusCode().value();
            if (upstreamStatus == 401 || upstreamStatus == 403) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "OpenProject auth failed. Check OPENPROJECT_AUTH_HEADER format/value.",
                ex
            );
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                "OpenProject request failed: HTTP " + upstreamStatus + " for path " + normalizedPath,
                    ex
            );
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "OpenProject is unreachable. Check OPENPROJECT_BASE_URL/network.",
                    ex
            );
        }

        Map body = response.getBody();
        if (body == null) {
            return Collections.emptyMap();
        }
        return (Map<String, Object>) body;
    }

    private Map<String, Object> fetchResourceFromHref(String href) {
        if (href == null || href.isBlank()) {
            return Collections.emptyMap();
        }

        String normalizedHref = href.trim();
        if (normalizedHref.startsWith("http://") || normalizedHref.startsWith("https://")) {
            if (normalizedHref.startsWith(baseUrl)) {
                String relative = normalizedHref.substring(baseUrl.length());
                String safeRelative = relative.startsWith("/") ? relative : "/" + relative;
                return fetchResource(safeRelative);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OpenProject link points outside configured base URL");
        }

        if (normalizedHref.startsWith("/api/v3")) {
            normalizedHref = normalizedHref.substring("/api/v3".length());
            if (normalizedHref.isBlank()) {
                normalizedHref = "/";
            }
        }

        String relativePath = normalizedHref.startsWith("/") ? normalizedHref : "/" + normalizedHref;
        return fetchResource(relativePath);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractElements(Map<String, Object> body) {
        Object embedded = body.get("_embedded");
        if (!(embedded instanceof Map<?, ?> embeddedMap)) {
            return Collections.emptyList();
        }
        Object elements = embeddedMap.get("elements");
        if (!(elements instanceof List<?> elementList)) {
            return Collections.emptyList();
        }
        return (List<Map<String, Object>>) (List<?>) elementList;
    }

    @Override
    public void syncProjects() {
        Map<String, Object> projectsResponse = fetchResource("/projects");
        log.info("Projects API response keys: {}", projectsResponse.keySet());
        
        // Check for total count
        Object totalObj = projectsResponse.get("total");
        log.info("Total projects in OpenProject: {}", totalObj);
        
        // Check for page size
        Object pageSizeObj = projectsResponse.get("pageSize");
        log.info("Page size: {}", pageSizeObj);
        
        List<Map<String, Object>> projects = extractElements(projectsResponse);
        log.info("Fetched {} projects from first page", projects.size());

        // STEP 2: LOOP PROJECTS
        int successCount = 0;
        int errorCount = 0;
        for (Map<String, Object> proj : projects) {

            Long projectId = ((Number) proj.get("id")).longValue();
            String projectName = (String) proj.get("name");

            Map<String, Object> projectDetail;
            try {
                projectDetail = fetchResource("/projects/" + projectId);
            } catch (ResponseStatusException ex) {
                log.warn("Skipping project {} because detail endpoint failed: {}", projectId, ex.getReason());
                continue;
            }

            // STEP 3: FETCH TASKS FOR EACH PROJECT
            List<Map<String, Object>> tasks;
            try {
                String workPackagesHref = null;
                Object linksObj = proj.get("_links");
                if (linksObj instanceof Map<?, ?> linksMap) {
                    Object workPackagesObj = linksMap.get("workPackages");
                    if (workPackagesObj instanceof Map<?, ?> workPackagesMap) {
                        Object hrefObj = workPackagesMap.get("href");
                        if (hrefObj instanceof String hrefValue) {
                            workPackagesHref = hrefValue;
                        }
                    }
                }

                if (workPackagesHref != null) {
                    tasks = extractElements(fetchResourceFromHref(workPackagesHref));
                } else {
                    String filterJson = "[{\"project\":{\"operator\":\"=\",\"values\":[\"" + projectId + "\"]}}]";
                    String taskPath = "/work_packages?filters=" + UriUtils.encodeQueryParam(filterJson, StandardCharsets.UTF_8);
                    tasks = extractElements(fetchResource(taskPath));
                }
            } catch (ResponseStatusException ex) {
                // Continue syncing other projects even if one work package call fails.
                log.warn("Skipping work_packages for project {} due to upstream error: {}", projectId, ex.getReason());
                tasks = Collections.emptyList();
            }

            List<Map<String, Object>> versions;
            try {
                versions = extractElements(fetchResource("/projects/" + projectId + "/versions"));
            } catch (ResponseStatusException ex) {
                log.warn("Skipping versions for project {} due to upstream error: {}", projectId, ex.getReason());
                versions = Collections.emptyList();
            }

            int totalTasks = tasks.size();
            int totalProgress = 0;
            String lead = "N/A";
            LocalDate dueDate = null;
            LinkedHashSet<String> teamMembers = new LinkedHashSet<>();

            // STEP 4: CALCULATE PROGRESS
            for (Map<String, Object> task : tasks) {

                Number percent = (Number) task.get("percentageDone");
                totalProgress += (percent != null ? percent.intValue() : 0);

                LocalDate taskDueDate = parseLocalDate((String) task.get("dueDate"));
                if (taskDueDate != null && (dueDate == null || taskDueDate.isAfter(dueDate))) {
                    dueDate = taskDueDate;
                }

                Map<String, Object> links = (Map<String, Object>) task.get("_links");
                if (links == null) {
                    continue;
                }

                if (links.get("assignee") != null) {
                    String assignee = (String) ((Map<?, ?>) links.get("assignee")).get("title");
                    if (assignee != null && !assignee.isBlank()) {
                        teamMembers.add(assignee);
                        if ("N/A".equals(lead)) {
                            lead = assignee;
                        }
                    }
                }
            }

            int avgProgress =
                totalTasks > 0 ? totalProgress / totalTasks : 0;

            String status =
                avgProgress == 100 ? "COMPLETE" :
                avgProgress > 50 ? "IN_PROGRESS" :
                "DELAYED";

            SprintSnapshot sprintSnapshot = buildSprintSnapshot(versions);

            String description = extractDescription(projectDetail);
            LocalDate startDate = parseLocalDateFromDateTime((String) projectDetail.get("createdAt"));
            String clientName = extractLinkTitle(projectDetail, "parent");
            if (clientName == null || clientName.isBlank()) {
                clientName = "OpenProject";
            }

            if (dueDate == null) {
                dueDate = parseLocalDate((String) projectDetail.get("dueDate"));
            }

            if (teamMembers.isEmpty() && !"N/A".equals(lead)) {
                teamMembers.add(lead);
            }

            // STEP 5: SAVE TO DB
            Project project = new Project();
            project.setId(projectId);
            project.setName(projectName);
            project.setClientName(clientName);
            project.setProgress(avgProgress);
            project.setStatus(status);
            project.setLead(lead);
            project.setStartDate(startDate);
            project.setDueDate(dueDate);
            project.setDescription(description);
            project.setTechStackCsv(""); // Tech stack not available in standard OpenProject - can be added via custom fields later
            project.setTeamCsv(String.join(",", teamMembers));
            project.setTotalPlannedSprints(sprintSnapshot.totalPlannedSprints());
            project.setCompletedSprints(sprintSnapshot.completedSprints());
            project.setActiveSprints(sprintSnapshot.activeSprints());
            project.setSprintNamesCsv(String.join(",", sprintSnapshot.names()));
            project.setSprintStatesCsv(String.join(",", sprintSnapshot.states()));

            projectRepository.save(project);
            successCount++;
            log.info("Successfully synced project {} ({})", projectId, projectName);
        }
        
        log.info("Sync complete. Success: {}, Errors: {}", successCount, errorCount);
    }

    private String extractDescription(Map<String, Object> projectDetail) {
        Object descriptionObj = projectDetail.get("description");
        if (descriptionObj instanceof Map<?, ?> descriptionMap) {
            Object raw = descriptionMap.get("raw");
            if (raw instanceof String rawString && !rawString.isBlank()) {
                return rawString;
            }
            Object html = descriptionMap.get("html");
            if (html instanceof String htmlString && !htmlString.isBlank()) {
                return htmlString;
            }
        }
        return "";
    }

    private String extractLinkTitle(Map<String, Object> body, String linkName) {
        Object linksObj = body.get("_links");
        if (!(linksObj instanceof Map<?, ?> linksMap)) {
            return null;
        }

        Object linkObj = linksMap.get(linkName);
        if (!(linkObj instanceof Map<?, ?> linkMap)) {
            return null;
        }

        Object titleObj = linkMap.get("title");
        if (titleObj instanceof String title) {
            return title;
        }
        return null;
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private LocalDate parseLocalDateFromDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(value).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private SprintSnapshot buildSprintSnapshot(List<Map<String, Object>> versions) {
        List<String> names = new ArrayList<>();
        List<String> states = new ArrayList<>();

        int completed = 0;
        int active = 0;

        for (Map<String, Object> version : versions) {
            String name = (String) version.get("name");
            if (name == null || name.isBlank()) {
                name = "Sprint " + (names.size() + 1);
            }

            String statusTitle = extractLinkTitle(version, "status");
            String normalized = normalizeSprintState(statusTitle);

            if ("COMPLETED".equals(normalized)) {
                completed++;
            } else if ("ACTIVE".equals(normalized)) {
                active++;
            }

            names.add(name.replace(",", " "));
            states.add(normalized);
        }

        return new SprintSnapshot(versions.size(), completed, active, names, states);
    }

    private String normalizeSprintState(String value) {
        if (value == null || value.isBlank()) {
            return "PLANNED";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("close") || normalized.contains("complete") || normalized.contains("done")) {
            return "COMPLETED";
        }
        if (normalized.contains("active") || normalized.contains("open") || normalized.contains("progress")) {
            return "ACTIVE";
        }
        return "PLANNED";
    }

    /**
     * Sync sprints (versions) from OpenProject for a specific project.
     * Fetches versions, work packages (tasks), and builds team member data.
     */
    public void syncSprints(Long projectId) {
        try {
            log.info("Starting sprint sync for project {}", projectId);
            
            // Fetch versions (sprints) for the project
            List<Map<String, Object>> versions = extractElements(fetchResource("/projects/" + projectId + "/versions"));
            log.info("Found {} versions (sprints) for project {}", versions.size(), projectId);
            
            if (versions.isEmpty()) {
                log.warn("Project {} has no versions defined in OpenProject", projectId);
                return;
            }
            
            // Also fetch all project tasks as fallback
            List<Map<String, Object>> allProjectTasks = fetchAllProjectTasks(projectId);
            log.info("Fetched {} total tasks for project {}", allProjectTasks.size(), projectId);
            
            int sprintCount = 0;
            for (Map<String, Object> version : versions) {
                Long sprintId = ((Number) version.get("id")).longValue();
                String sprintName = (String) version.get("name");
                String status = normalizeSprintState(extractLinkTitle(version, "status"));
                LocalDate startDate = parseLocalDate((String) version.get("startDate"));
                LocalDate endDate = parseLocalDate((String) version.get("endDate"));
                String description = extractDescription(version);

                // Fetch tasks for this version
                List<Map<String, Object>> tasks = fetchTasksForVersion(sprintId, projectId);

                // Calculate sprint stats from tasks
                SprintStats stats = calculateSprintStats(tasks);

                // Save or update sprint
                Sprint sprint = new Sprint();
                sprint.setId(sprintId);
                sprint.setProjectId(projectId);
                sprint.setName(sprintName);
                sprint.setGoal(description);
                sprint.setStatus(status);
                sprint.setStartDate(startDate);
                sprint.setEndDate(endDate);
                sprint.setScrumMaster(stats.scrumMaster);
                sprint.setProgress(calculateProgress(tasks));
                sprint.setTotalTasks(stats.total);
                sprint.setCompletedTasks(stats.completed);
                sprint.setInProgressTasks(stats.inProgress);
                sprint.setTodoTasks(stats.todo);
                sprint.setTestingTasks(stats.testing);
                sprint.setStoryPoints(stats.storyPoints);
                sprint.setBugsFixed(stats.bugsFixed);
                sprint.setEstimatedHours(stats.estimatedHours);
                sprint.setActualHours(stats.actualHours);
                
                sprintRepository.save(sprint);

                // Sync tasks for this sprint
                syncSprintTasks(sprintId, tasks);

                // Sync team members
                syncTeamMembers(sprintId, tasks);
                sprintCount++;
            }
            
            log.info("Synced {} sprints for project {}", sprintCount, projectId);
        } catch (Exception ex) {
            log.error("Error syncing sprints for project {}: {}", projectId, ex.getMessage(), ex);
        }
    }

    private List<Map<String, Object>> fetchTasksForVersion(Long versionId, Long projectId) {
        // Fetch all project tasks and filter by version in Java
        List<Map<String, Object>> allTasks = fetchAllProjectTasks(projectId);
        
        // Filter tasks that belong to this version (extract ID from href)
        List<Map<String, Object>> versionTasks = new ArrayList<>();
        for (Map<String, Object> task : allTasks) {
            Long taskVersionId = extractLinkId(task, "version");
            if (taskVersionId != null && taskVersionId.equals(versionId)) {
                versionTasks.add(task);
            }
        }
        
        log.info("Found {} tasks for version {} out of {} total tasks", 
                versionTasks.size(), versionId, allTasks.size());
        return versionTasks;
    }

    private Long extractLinkId(Map<String, Object> body, String linkName) {
        Object linksObj = body.get("_links");
        if (!(linksObj instanceof Map<?, ?> linksMap)) {
            return null;
        }

        Object linkObj = linksMap.get(linkName);
        if (!(linkObj instanceof Map<?, ?> linkMap)) {
            return null;
        }

        Object hrefObj = linkMap.get("href");
        if (!(hrefObj instanceof String href)) {
            return null;
        }

        // Extract ID from href like "/api/v3/versions/7"
        try {
            String[] parts = href.split("/");
            String lastPart = parts[parts.length - 1];
            // Remove any query params
            if (lastPart.contains("?")) {
                lastPart = lastPart.substring(0, lastPart.indexOf("?"));
            }
            return Long.parseLong(lastPart);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<Map<String, Object>> fetchAllProjectTasks(Long projectId) {
        try {
            // Use project work packages link if available
            Map<String, Object> projectDetail = fetchResource("/projects/" + projectId);
            
            Object linksObj = projectDetail.get("_links");
            if (linksObj instanceof Map<?, ?> linksMap) {
                Object workPackagesObj = linksMap.get("workPackages");
                if (workPackagesObj instanceof Map<?, ?> workPackagesMap) {
                    Object hrefObj = workPackagesMap.get("href");
                    if (hrefObj instanceof String hrefValue) {
                        log.info("Fetching tasks from workPackages link for project {}", projectId);
                        return extractElements(fetchResourceFromHref(hrefValue));
                    }
                }
            }
            
            // Fallback: fetch all work packages without filters
            log.info("Fetching all work packages for project {}", projectId);
            return extractElements(fetchResource("/work_packages"));
        } catch (Exception ex) {
            log.warn("Failed to fetch tasks for project {}: {}", projectId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private void syncSprintTasks(Long sprintId, List<Map<String, Object>> tasks) {
        for (Map<String, Object> task : tasks) {
            try {
                Long taskId = ((Number) task.get("id")).longValue();
                String title = (String) task.get("subject");
                String status = extractLinkTitle(task, "status");
                String type = extractLinkTitle(task, "type");
                Number storyPoints = (Number) task.get("storyPoints"); // Custom field
                Number percentageDone = (Number) task.get("percentageDone");
                Double estimatedHours = parseHours(task.get("estimatedTime"));
                Double actualHours = parseHours(task.get("spentTime"));

                // Get assignee
                String assignee = "Unassigned";
                Map<String, Object> links = (Map<String, Object>) task.get("_links");
                if (links != null && links.get("assignee") != null) {
                    assignee = (String) ((Map<?, ?>) links.get("assignee")).get("title");
                }

                SprintTask sprintTask = new SprintTask();
                sprintTask.setId(taskId);
                sprintTask.setSprintId(sprintId);
                sprintTask.setTitle(title);
                sprintTask.setStatus(status != null ? status : "New");
                sprintTask.setType(type != null ? type : "Task");
                sprintTask.setStoryPoints(storyPoints != null ? storyPoints.intValue() : 0);
                sprintTask.setAssignee(assignee);
                sprintTask.setEstimatedHours(estimatedHours);
                sprintTask.setActualHours(actualHours);
                sprintTask.setProgressPercentage(percentageDone != null ? percentageDone.intValue() : 0);

                sprintTaskRepository.save(sprintTask);
            } catch (Exception ex) {
                log.warn("Error syncing task: {}", ex.getMessage());
            }
        }
    }

    private void syncTeamMembers(Long sprintId, List<Map<String, Object>> tasks) {
        log.info("Syncing team members for sprint {} with {} tasks", sprintId, tasks.size());
        
        // Group tasks by assignee
        Map<String, List<Map<String, Object>>> tasksByAssignee = new HashMap<>();
        
        for (Map<String, Object> task : tasks) {
            String assignee = "Unassigned";
            Map<String, Object> links = (Map<String, Object>) task.get("_links");
            if (links != null && links.get("assignee") != null) {
                Object assigneeObj = links.get("assignee");
                if (assigneeObj instanceof Map<?, ?> assigneeMap) {
                    Object titleObj = assigneeMap.get("title");
                    if (titleObj instanceof String) {
                        assignee = (String) titleObj;
                    }
                }
            }
            tasksByAssignee.computeIfAbsent(assignee, k -> new ArrayList<>()).add(task);
        }
        
        log.info("Found {} unique assignees for sprint {}", tasksByAssignee.size(), sprintId);

        // Create team member records
        int memberIndex = 0;
        for (Map.Entry<String, List<Map<String, Object>>> entry : tasksByAssignee.entrySet()) {
            String name = entry.getKey();
            List<Map<String, Object>> memberTasks = entry.getValue();

            int total = memberTasks.size();
            int completed = 0;
            int inProgress = 0;
            int todo = 0;
            double estimatedHours = 0;
            double actualHours = 0;

            for (Map<String, Object> task : memberTasks) {
                String status = extractLinkTitle(task, "status");
                if (isCompletedStatus(status)) {
                    completed++;
                } else if (isInProgressStatus(status)) {
                    inProgress++;
                } else {
                    todo++;
                }

                Double est = parseHours(task.get("estimatedTime"));
                Double act = parseHours(task.get("spentTime"));
                if (est != null) estimatedHours += est;
                if (act != null) actualHours += act;
            }

            TeamMember member = new TeamMember();
            member.setId(sprintId * 1000 + memberIndex++); // Generate unique ID
            member.setSprintId(sprintId);
            member.setName(name);
            member.setRole("Team Member"); // Can be enhanced with role lookup
            member.setAssignedTasks(total);
            member.setCompletedTasks(completed);
            member.setInProgressTasks(inProgress);
            member.setTodoTasks(todo);
            member.setEstimatedHours(estimatedHours);
            member.setActualHours(actualHours);

            teamMemberRepository.save(member);
            log.info("Saved team member: {} with {} tasks", name, total);
        }
        
        log.info("Total team members saved for sprint {}: {}", sprintId, memberIndex);
    }

    private SprintStats calculateSprintStats(List<Map<String, Object>> tasks) {
        SprintStats stats = new SprintStats();
        Set<String> scrumMasters = new HashSet<>();

        for (Map<String, Object> task : tasks) {
            stats.total++;
            
            String status = extractLinkTitle(task, "status");
            String type = extractLinkTitle(task, "type");
            
            if (isCompletedStatus(status)) {
                stats.completed++;
            } else if (isInProgressStatus(status)) {
                stats.inProgress++;
            } else if (isTestingStatus(status)) {
                stats.testing++;
            } else {
                stats.todo++;
            }

            if ("Bug".equalsIgnoreCase(type) && isCompletedStatus(status)) {
                stats.bugsFixed++;
            }

            // Try to get story points (custom field)
            Number sp = (Number) task.get("storyPoints");
            if (sp != null) {
                stats.storyPoints += sp.intValue();
            }

            // Accumulate hours
            Double est = parseHours(task.get("estimatedTime"));
            Double act = parseHours(task.get("spentTime"));
            if (est != null) stats.estimatedHours += est;
            if (act != null) stats.actualHours += act;

            // Get assignee for potential scrum master (first assignee or most tasks)
            Map<String, Object> links = (Map<String, Object>) task.get("_links");
            if (links != null && links.get("assignee") != null) {
                String assignee = (String) ((Map<?, ?>) links.get("assignee")).get("title");
                if (assignee != null && !assignee.isBlank()) {
                    scrumMasters.add(assignee);
                }
            }
        }

        // Set scrum master (first assignee found, or "TBD")
        stats.scrumMaster = scrumMasters.isEmpty() ? "TBD" : scrumMasters.iterator().next();
        
        return stats;
    }

    private int calculateProgress(List<Map<String, Object>> tasks) {
        if (tasks.isEmpty()) return 0;
        
        int totalProgress = 0;
        for (Map<String, Object> task : tasks) {
            Number percentageDone = (Number) task.get("percentageDone");
            totalProgress += (percentageDone != null ? percentageDone.intValue() : 0);
        }
        return totalProgress / tasks.size();
    }

    private Double parseHours(Object timeValue) {
        if (timeValue == null) return null;
        
        try {
            if (timeValue instanceof String) {
                // Parse ISO 8601 duration format (e.g., "PT8H" for 8 hours)
                String timeStr = (String) timeValue;
                if (timeStr.startsWith("PT")) {
                    double hours = 0;
                    if (timeStr.contains("H")) {
                        String hourPart = timeStr.substring(2, timeStr.indexOf("H"));
                        hours = Double.parseDouble(hourPart);
                    }
                    if (timeStr.contains("M")) {
                        int minIndex = timeStr.indexOf("H");
                        if (minIndex < 0) minIndex = 1;
                        String minPart = timeStr.substring(minIndex + 1, timeStr.indexOf("M"));
                        hours += Double.parseDouble(minPart) / 60.0;
                    }
                    return hours;
                }
                return Double.parseDouble((String) timeValue);
            }
            if (timeValue instanceof Number) {
                return ((Number) timeValue).doubleValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("closed") || s.contains("completed") || s.contains("done");
    }

    private boolean isInProgressStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("in progress") || s.contains("working") || s.contains("ongoing");
    }

    private boolean isTestingStatus(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("testing") || s.contains("test") || s.contains("qa");
    }

    private static class SprintStats {
        int total = 0;
        int completed = 0;
        int inProgress = 0;
        int todo = 0;
        int testing = 0;
        int storyPoints = 0;
        int bugsFixed = 0;
        double estimatedHours = 0;
        double actualHours = 0;
        String scrumMaster = "TBD";
    }

    private record SprintSnapshot(
        int totalPlannedSprints,
        int completedSprints,
        int activeSprints,
        List<String> names,
        List<String> states
    ) {
    }
}
