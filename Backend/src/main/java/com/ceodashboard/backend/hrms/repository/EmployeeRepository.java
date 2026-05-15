package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    
    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    Optional<Employee> findByOfficialEmail(String officialEmail);
    
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department LEFT JOIN FETCH e.designation WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    @Query("SELECT e FROM Employee e WHERE e.reportingManager.id = :managerId")
    Page<Employee> findByReportingManagerId(@Param("managerId") Long managerId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    Page<Employee> findAll(Pageable pageable);
    
    long countByEmployeeStatus(String employeeStatus);
}
