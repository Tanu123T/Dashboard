package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
    List<WorkExperience> findByEmployeeIdOrderByStartDateDesc(Long employeeId);
}
