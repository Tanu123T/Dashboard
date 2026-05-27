package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "appraisal_review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "reporting_officer")
    private String reportingOfficer;
    
    @Column(name = "ro_designation")
    private String roDesignation;

    @Column(name = "status")
    private String status;
    
    @Column(name = "appraisal_status")
    private String appraisalStatus;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "last_modified")
    private Instant updatedAt;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
