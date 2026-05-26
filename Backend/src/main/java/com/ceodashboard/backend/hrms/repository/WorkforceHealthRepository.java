package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface WorkforceHealthRepository
                extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

        /**
         * Count employees present today (checked in)
         */
        @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                        "WHERE DATE(date) = CURDATE() AND has_checked_in = 1", nativeQuery = true)
        Long countEmployeePresentToday();

        /**
         * Count employees not checked in today (absent/on leave/not arrived)
         */
        @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                        "WHERE DATE(date) = CURDATE() AND (has_checked_in = 0 OR status IN ('ABSENT', 'ON_LEAVE'))", nativeQuery = true)
        Long countEmployeesOnLeaveToday();

        /**
         * Count late arrivals - using status column (LATE if recorded)
         */
        @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                        "WHERE DATE(date) = CURDATE() AND status = 'LATE'", nativeQuery = true)
        Long countLateArrivals();

        /**
         * Count employees currently in office (checked in, not on leave/absent)
         */
        @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                        "WHERE DATE(date) = CURDATE() AND has_checked_in = 1 " +
                        "AND status NOT IN ('ON_LEAVE', 'ABSENT', 'HALF_DAY')", nativeQuery = true)
        Long countPresentInOffice();

        /**
         * Calculate overall attendance consistency
         */
        @Query(value = "SELECT ROUND(AVG(present_ratio) * 100, 2) FROM (" +
                        "SELECT employee_id, " +
                        "CAST(SUM(CASE WHEN has_checked_in = 1 THEN 1 ELSE 0 END) AS DECIMAL) / COUNT(*) as present_ratio "
                        +
                        "FROM attendance " +
                        "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                        "GROUP BY employee_id" +
                        ") as subquery", nativeQuery = true)
        Double calculateAttendanceConsistency();

        /**
         * Get attendance trend by month
         */
        @Query(value = "SELECT " +
                        "DATE_FORMAT(date, '%Y-%m') as month, " +
                        "COUNT(DISTINCT employee_id) as headcount " +
                        "FROM attendance " +
                        "WHERE has_checked_in = 1 " +
                        "GROUP BY DATE_FORMAT(date, '%Y-%m') " +
                        "ORDER BY DATE_FORMAT(date, '%Y-%m') DESC " +
                        "LIMIT 12", nativeQuery = true)
        List<Map<String, Object>> getHeadcountTrendByMonth();

        /**
         * Get at-risk employees (recently absent or inconsistent)
         */
        @Query(value = "SELECT a.* FROM attendance a " +
                        "WHERE a.date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "AND (a.has_checked_in = 0 OR a.status IN ('ABSENT', 'ON_LEAVE', 'LATE')) " +
                        "ORDER BY a.date DESC " +
                        "LIMIT 50", nativeQuery = true)
        List<Attendance> getAtRiskEmployees();

        /**
         * Get attendance log with optional filters
         */
        @Query(value = "SELECT a.* FROM attendance a " +
                        "WHERE (:searchTerm IS NULL OR a.employee_id LIKE CONCAT('%', :searchTerm, '%')) " +
                        "AND (:departmentId IS NULL OR a.employee_id IN (SELECT id FROM employee WHERE department_id = :departmentId)) "
                        +
                        "AND (:fromDate IS NULL OR a.date >= :fromDate) " +
                        "AND (:toDate IS NULL OR a.date <= :toDate) " +
                        "ORDER BY a.date DESC", nativeQuery = true)
        List<Attendance> getFilteredAttendanceLog(
                        @Param("searchTerm") String searchTerm,
                        @Param("departmentId") Long departmentId,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate);
}
