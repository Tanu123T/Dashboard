package com.ceodashboard.backend.service;

import java.util.Map;

public interface OpenProjectService {
    void syncProjects();
    Map<String, Object> fetchResource(String path);
}
