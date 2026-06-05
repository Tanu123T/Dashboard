package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.AppraisalEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppraisalEvaluationRepository extends JpaRepository<AppraisalEvaluation, Long> {
    @Query("SELECT COALESCE(AVG(a.scoredPoints), 0) FROM AppraisalEvaluation a WHERE a.employee.id = :employeeId")
    Double findAverageScoreByEmployeeId(@Param("employeeId") Long employeeId);
}
