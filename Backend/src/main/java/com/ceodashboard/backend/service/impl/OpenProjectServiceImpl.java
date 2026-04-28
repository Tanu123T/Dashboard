package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.repository.ProjectRepository;
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
        List<Map<String, Object>> projects = extractElements(fetchResource("/projects"));

        // STEP 2: LOOP PROJECTS
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
        }
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

    private record SprintSnapshot(
        int totalPlannedSprints,
        int completedSprints,
        int activeSprints,
        List<String> names,
        List<String> states
    ) {
    }
}
