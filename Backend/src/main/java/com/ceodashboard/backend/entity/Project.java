package com.ceodashboard.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "project_id")
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "project_name", length = 255, nullable = false)
    private String name;

    @Column(name = "project_progress")
    private Float progress;

    @Column(name = "project_description", length = 500)
    private String description;

    @Column(name = "project_status", length = 50)
    private String status;

    @Column(name = "team_lead", length = 100)
    private String lead;

    @Transient
    private String clientName;

    @Transient
    private LocalDate startDate;

    @Transient
    private LocalDate dueDate;

    @Transient
    private Integer totalPlannedSprints;

    @Transient
    private String techStackCsv;

    @Transient
    private String teamCsv;

    @Transient
    private Integer completedSprints;

    @Transient
    private Integer activeSprints;

    @Transient
    private String sprintNamesCsv;

    @Transient
    private String sprintStatesCsv;
}
