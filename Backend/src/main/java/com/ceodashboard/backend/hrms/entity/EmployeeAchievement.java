package com.ceodashboard.backend.hrms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDate;

@Entity
@Table(name = "employee_achievement")
@SQLRestriction("company_id = 1")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "achievement_title")
    private String achievementTitle;

    @Column(name = "achievement_description")
    private String achievementDescription;

    @Column(name = "achievement_date")
    private LocalDate achievementDate;

    @Column(name = "company_id")
    private Long companyId;
}
