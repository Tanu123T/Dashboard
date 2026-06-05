package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.dto.PerformanceTrendDTO;
import com.ceodashboard.backend.hrms.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    @Query("SELECT new com.ceodashboard.backend.hrms.dto.PerformanceTrendDTO(" +
           "FUNCTION('DATE_FORMAT', pr.appraisalDate, '%b'), " +
           "ROUND(AVG(COALESCE(pr.targetAchived,0)),0) ) " +
           "FROM PerformanceReview pr " +
           "WHERE pr.appraisalReview.employee.id = :employeeId " +
           "GROUP BY FUNCTION('DATE_FORMAT', pr.appraisalDate, '%Y-%m'), FUNCTION('DATE_FORMAT', pr.appraisalDate, '%b') " +
           "ORDER BY FUNCTION('DATE_FORMAT', pr.appraisalDate, '%Y-%m') ASC")
    List<PerformanceTrendDTO> findPerformanceTrendsByEmployeeId(@Param("employeeId") Long employeeId);
}
