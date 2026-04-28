package com.ceodashboard.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sprint_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintTask {

    @Id
    private Long id;

    private Long sprintId;

    private String title;

    private String status;

    private String type;

    private Integer storyPoints;

    private String assignee;

    private Double estimatedHours;

    private Double actualHours;

    private Integer progressPercentage;
}
