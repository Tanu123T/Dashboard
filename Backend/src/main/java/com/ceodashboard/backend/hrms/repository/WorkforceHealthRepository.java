package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Workforce health repository using pure JPA / Hibernate queries.
 */
@Repository
public interface WorkforceHealthRepository
        extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a " +
           "WHERE a.attendanceDate = :date AND a.hasCheckedIn = true")
    Long countDistinctPresentToday(@Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a " +
           "WHERE a.attendanceDate = :date " +
           "AND (a.hasCheckedIn = false OR a.status IN :statuses)")
    Long countDistinctOnLeaveToday(@Param("date") LocalDate date,
                                   @Param("statuses") List<String> statuses);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a " +
           "WHERE a.attendanceDate = :date AND a.status = :status")
    Long countDistinctByStatusOnDate(@Param("date") LocalDate date,
                                    @Param("status") String status);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a " +
           "WHERE a.attendanceDate = :date " +
           "AND a.hasCheckedIn = true " +
           "AND a.status NOT IN :excludedStatuses")
    Long countDistinctPresentInOffice(@Param("date") LocalDate date,
                                      @Param("excludedStatuses") List<String> excludedStatuses);

    @EntityGraph(attributePaths = {"employee", "employee.department"})
    @Query("SELECT a FROM Attendance a " +
           "WHERE a.attendanceDate >= :fromDate " +
           "AND (a.hasCheckedIn = false OR a.status IN :statuses) " +
           "ORDER BY a.attendanceDate DESC")
    List<Attendance> findAtRiskFromDate(@Param("fromDate") LocalDate fromDate,
                                       @Param("statuses") List<String> statuses);

    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);
}
