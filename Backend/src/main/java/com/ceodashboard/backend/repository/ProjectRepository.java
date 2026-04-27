package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	long countByStatus(String status);
	List<Project> findAllByOrderByNameAsc();
}
