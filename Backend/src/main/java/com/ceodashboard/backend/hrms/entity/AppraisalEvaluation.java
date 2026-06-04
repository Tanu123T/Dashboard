package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "appraisal_evaluation")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "scored_points")
    private Double scoredPoints;

    @Column(name = "company_id")
    private Long companyId;
}
