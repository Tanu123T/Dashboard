package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.Instant;

@Entity
@Table(name = "employee_skill")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "skill_level")
    private String skillLevel;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "company_id")
    private Long companyId;
}
