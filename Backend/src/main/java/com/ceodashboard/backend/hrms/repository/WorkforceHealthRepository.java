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

/**
 * All native queries filtered to company_id = 1 (smarthrms tenant).
 */
@Repository
public interface WorkforceHealthRepository
        extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                   "WHERE DATE(date) = CURDATE() AND has_checked_in = 1 AND company_id = 1",
           nativeQuery = true)
    Long countEmployeePresentToday();

    @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                   "WHERE DATE(date) = CURDATE() " +
                   "AND (has_checked_in = 0 OR status IN ('ABSENT','ON_LEAVE')) " +
                   "AND company_id = 1",
           nativeQuery = true)
    Long countEmployeesOnLeaveToday();

    @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                   "WHERE DATE(date) = CURDATE() AND status = 'LATE' AND company_id = 1",
           nativeQuery = true)
    Long countLateArrivals();

    @Query(value = "SELECT COUNT(DISTINCT employee_id) FROM attendance " +
                   "WHERE DATE(date) = CURDATE() AND has_checked_in = 1 " +
                   "AND status NOT IN ('ON_LEAVE','ABSENT','HALF_DAY') AND company_id = 1",
           nativeQuery = true)
    Long countPresentInOffice();

    @Query(value = "SELECT ROUND(AVG(present_ratio) * 100, 2) FROM (" +
                   "  SELECT employee_id, " +
                   "  CAST(SUM(CASE WHEN has_checked_in=1 THEN 1 ELSE 0 END) AS DECIMAL)/COUNT(*) AS present_ratio " +
                   "  FROM attendance " +
                   "  WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND company_id = 1 " +
                   "  GROUP BY employee_id" +
                   ") AS subquery",
           nativeQuery = true)
    Double calculateAttendanceConsistency();

    @Query(value = "SELECT DATE_FORMAT(date,'%Y-%m') AS month, COUNT(DISTINCT employee_id) AS headcount " +
                   "FROM attendance " +
                   "WHERE has_checked_in = 1 AND company_id = 1 " +
                   "GROUP BY DATE_FORMAT(date,'%Y-%m') " +
                   "ORDER BY DATE_FORMAT(date,'%Y-%m') DESC LIMIT 12",
           nativeQuery = true)
    List<Map<String, Object>> getHeadcountTrendByMonth();

    @Query(value = "SELECT a.id, a.employee_id, a.date AS attendanceDate, a.status, " +
                   "COALESCE(e.id,0) AS emp_id, COALESCE(d.name,'Unknown') AS dept_name " +
                   "FROM attendance a " +
                   "LEFT JOIN employee   e ON a.employee_id = e.id AND e.company_id = 1 " +
                   "LEFT JOIN department d ON e.department_id = d.id AND d.company_id = 1 " +
                   "WHERE a.date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                   "AND (a.has_checked_in = 0 OR a.status IN ('ABSENT','ON_LEAVE','LATE')) " +
                   "AND a.company_id = 1 " +
                   "ORDER BY a.date DESC LIMIT 50",
           nativeQuery = true)
    List<Map<String, Object>> getAtRiskEmployees();

    @Query(value = "SELECT a.id, a.employee_id, a.date AS attendanceDate, a.has_checked_in, " +
                   "a.status, a.hours AS work_hours, NULL AS checkout_time, " +
                   "COALESCE(e.id,0) AS emp_id, COALESCE(d.name,'Unknown') AS dept_name " +
                   "FROM attendance a " +
                   "LEFT JOIN employee   e ON a.employee_id = e.id AND e.company_id = 1 " +
                   "LEFT JOIN department d ON e.department_id = d.id AND d.company_id = 1 " +
                   "WHERE (:searchTerm IS NULL OR e.emp_unique_id LIKE CONCAT('%',:searchTerm,'%') " +
                   "       OR CONCAT(e.first_name,' ',e.last_name) LIKE CONCAT('%',:searchTerm,'%')) " +
                   "AND (:departmentId IS NULL OR d.id = :departmentId) " +
                   "AND (:fromDate IS NULL OR DATE(a.date) >= :fromDate) " +
                   "AND (:toDate   IS NULL OR DATE(a.date) <= :toDate) " +
                   "AND a.company_id = 1 " +
                   "ORDER BY a.date DESC",
           nativeQuery = true)
    List<Map<String, Object>> getFilteredAttendanceLog(
            @Param("searchTerm")   String    searchTerm,
            @Param("departmentId") Long      departmentId,
            @Param("fromDate")     LocalDate fromDate,
            @Param("toDate")       LocalDate toDate);
}
