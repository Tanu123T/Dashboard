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

/**
 * JPA repository for Employee.
 *
 * The Employee entity carries @SQLRestriction("company_id = 1") so Hibernate
 * automatically appends that condition to EVERY generated SQL — no manual
 * WHERE clause needed here.
 */
public interface EmployeeRepository
        extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    // @SQLRestriction automatically adds AND company_id = 1 to all queries below.

    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    Optional<Employee> findByOfficialEmail(String officialEmail);

    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    Optional<Employee> findByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.department " +
           "LEFT JOIN FETCH e.designation " +
           "LEFT JOIN FETCH e.reportingManager " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"department", "designation", "reportingManager"})
    @Query("SELECT e FROM Employee e WHERE e.reportingManager.id = :managerId")
    Page<Employee> findByReportingManagerId(@Param("managerId") Long managerId, Pageable pageable);

    @EntityGraph(attributePaths = {"department", "designation", "reportingManager","branch","branch.region"})
    Page<Employee> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "designation", "reportingManager","branch","branch.region"})
    Page<Employee> findAllWithDetails(Pageable pageable);

    long countByEmployeeStatus(String employeeStatus);
}
