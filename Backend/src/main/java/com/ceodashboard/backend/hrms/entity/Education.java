package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "education")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "education_type")
    private String educationType;

    @Column(name = "subject")
    private String subject;

    @Column(name = "institution")
    private String institution;

    @Column(name = "start_year")
    private LocalDate startYear;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "grade")
    private String grade;

    @Column(name = "description")
    private String description;

    @Column(name = "company_id")
    private Long companyId;
}
