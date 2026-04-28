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
@Table(name = "sprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {

    @Id
    private Long id;

    private Long projectId;

    private String name;

    @Column(length = 2000)
    private String goal;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private String scrumMaster;

    private Integer progress;

    private Integer totalTasks;

    private Integer completedTasks;

    private Integer inProgressTasks;

    private Integer todoTasks;

    private Integer testingTasks;

    private Integer storyPoints;

    private Integer bugsFixed;

    private Double estimatedHours;

    private Double actualHours;

    @Column(length = 4000)
    private String burndownDataJson;
}
