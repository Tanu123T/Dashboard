package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Long> {
    List<EmployeeCertification> findByEmployeeIdOrderByCertificationDateDesc(Long employeeId);
}
