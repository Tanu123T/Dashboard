package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "performance_review")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appraisal_review_id")
    private AppraisalReview appraisalReview;

    @Column(name = "appraisal_date")
    private LocalDate appraisalDate;

    @Column(name = "target_achived")
    private Double targetAchived;

    @Column(name = "company_id")
    private Long companyId;
}
