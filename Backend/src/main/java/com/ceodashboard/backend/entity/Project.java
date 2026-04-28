package com.ceodashboard.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    private Long id;

    private String name;

    private String clientName;

    private Integer progress;

    private String status;

    private String lead;

    private LocalDate startDate;

    private LocalDate dueDate;

    @Column(length = 4000)
    private String description;

    @Column(length = 2000)
    private String techStackCsv;

    @Column(length = 2000)
    private String teamCsv;

    private Integer totalPlannedSprints;

    private Integer completedSprints;

    private Integer activeSprints;

    @Column(length = 2000)
    private String sprintNamesCsv;

    @Column(length = 2000)
    private String sprintStatesCsv;
}
