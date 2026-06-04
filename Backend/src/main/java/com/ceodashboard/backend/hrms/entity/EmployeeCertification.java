package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "employee_certification")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "certification_name")
    private String certificationName;

    @Column(name = "issuing_organization")
    private String issuingOrganization;

    @Column(name = "certification_date")
    private LocalDate certificationDate;

    @Column(name = "company_id")
    private Long companyId;
}
