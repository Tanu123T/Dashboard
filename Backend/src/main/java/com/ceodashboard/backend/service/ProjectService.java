package com.ceodashboard.backend.service;

import com.ceodashboard.backend.dto.ProjectDTO;
import com.ceodashboard.backend.dto.ProjectsPageResponseDTO;

public interface ProjectService {
    
    /**
     * Get consolidated project page data including summary and all projects.
     * This is a single endpoint that consolidates data from multiple sources.
     */
    ProjectsPageResponseDTO getProjectsPageData();
    
    /**
     * Get detailed project information including sprint timeline.
     * This consolidates project details, team, tech stack, and sprint data.
     */
    ProjectDTO getProjectDetail(Long projectId);
}
