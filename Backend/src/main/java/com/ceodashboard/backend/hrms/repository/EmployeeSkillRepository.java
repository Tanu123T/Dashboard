package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
