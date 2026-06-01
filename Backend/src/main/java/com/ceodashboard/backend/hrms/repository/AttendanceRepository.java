package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * The Attendance entity carries @SQLRestriction("company_id = 1") so Hibernate
 * automatically appends company_id = 1 to every generated SQL.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeIdAndDateRange(
            @Param("employeeId") Long      employeeId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate);

    long countByEmployeeIdAndStatus(Long employeeId, String status);
}
