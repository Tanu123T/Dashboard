package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    long countByOrgId(Integer orgId);
    long countByOrgIdAndStatus(Integer orgId, String status);
    List<Project> findAllByOrgIdOrderByNameAsc(Integer orgId);
    Optional<Project> findByIdAndOrgId(Long id, Integer orgId);
}
