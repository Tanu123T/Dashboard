package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.TechvgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TechvgUserRepository extends JpaRepository<TechvgUser, Long> {
    Optional<TechvgUser> findFirstByEmployeeIdOrderByIdDesc(Long employeeId);
}
