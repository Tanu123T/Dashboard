package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.EmployeeProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, Long> {
    List<EmployeeProject> findByEmployeeIdOrderByStartDateDesc(Long employeeId);
}
