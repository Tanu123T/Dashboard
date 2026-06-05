package com.ceodashboard.backend.config;

import com.ceodashboard.backend.entity.Project;
import com.ceodashboard.backend.repository.ProjectRepository;
import com.ceodashboard.backend.service.OpenProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenProjectStartupSync implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenProjectStartupSync.class);
    private static final Integer DEFAULT_ORG_ID = 1;

    private final OpenProjectService openProjectService;
    private final ProjectRepository projectRepository;

    public OpenProjectStartupSync(OpenProjectService openProjectService,
                                  ProjectRepository projectRepository) {
        this.openProjectService = openProjectService;
        this.projectRepository = projectRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existingProjectCount = projectRepository.countByOrgId(DEFAULT_ORG_ID);
        if (existingProjectCount > 0) {
            log.info("OpenProject startup sync skipped: {} projects already exist for org {}.", existingProjectCount, DEFAULT_ORG_ID);
            return;
        }

        log.info("OpenProject startup sync starting because no projects exist for org {}.", DEFAULT_ORG_ID);
        try {
            openProjectService.syncProjects();
            List<Project> projects = projectRepository.findAllByOrgIdOrderByNameAsc(DEFAULT_ORG_ID);
            for (Project project : projects) {
                try {
                    openProjectService.syncSprints(project.getId());
                } catch (Exception ex) {
                    log.warn("Failed to sync sprints for project {} during startup sync: {}", project.getId(), ex.getMessage(), ex);
                }
            }
            log.info("OpenProject startup sync completed. Imported {} projects.", projects.size());
        } catch (Exception ex) {
            log.error("OpenProject startup sync failed.", ex);
        }
    }
}
