package com.ceodashboard.backend.service;

import java.util.Map;

public interface OpenProjectService {
    void syncProjects();
    void syncSprints(Long projectId);
    Map<String, Object> fetchResource(String path);
}
