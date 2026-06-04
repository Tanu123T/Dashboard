package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.entity.AppraisalReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppraisalReviewRepository extends JpaRepository<AppraisalReview, Long> {
    Optional<AppraisalReview> findFirstByEmployeeIdOrderByIdDesc(Long employeeId);
}
