package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.EmployeeAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeAchievementRepository extends JpaRepository<EmployeeAchievement, Long> {
    List<EmployeeAchievement> findByEmployeeIdOrderByAchievementDateDesc(Long employeeId);
}
