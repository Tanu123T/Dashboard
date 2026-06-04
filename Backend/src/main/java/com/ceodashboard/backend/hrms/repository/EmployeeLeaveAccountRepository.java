package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.EmployeeLeaveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeLeaveAccountRepository extends JpaRepository<EmployeeLeaveAccount, Long> {
    Optional<EmployeeLeaveAccount> findFirstByEmployeeIdOrderByIdDesc(Long employeeId);
}
