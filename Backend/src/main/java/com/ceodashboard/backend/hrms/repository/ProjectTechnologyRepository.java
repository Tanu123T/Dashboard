package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.ProjectTechnology;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectTechnologyRepository extends JpaRepository<ProjectTechnology, Long> {
    List<ProjectTechnology> findByProjectId(Long projectId);
}
