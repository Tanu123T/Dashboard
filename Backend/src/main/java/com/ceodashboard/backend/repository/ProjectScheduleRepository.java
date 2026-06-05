package com.ceodashboard.backend.repository;

import com.ceodashboard.backend.entity.ProjectSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectScheduleRepository extends JpaRepository<ProjectSchedule, Long> {
    Optional<ProjectSchedule> findByProjectIdAndOrgId(Long projectId, Integer orgId);
}
