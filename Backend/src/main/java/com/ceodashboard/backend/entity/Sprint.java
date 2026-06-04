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
import java.time.LocalDateTime;

@Entity
@Table(name = "sprint")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "sprint_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "sprint_name", nullable = false, length = 255)
    private String name;

    @Column(name = "sprint_goal", length = 500)
    private String goal;

    @Column(name = "sprint_status", length = 50)
    private String status;

    @Column(name = "sprint_start_date")
    private LocalDate startDate;

    @Column(name = "sprint_end_date")
    private LocalDate endDate;

    @Column(name = "scrum_master", length = 100)
    private String scrumMaster;

    @Column(name = "sprint_progress")
    private Float progress;

    @Column(name = "total_tasks")
    private Integer totalTasks;

    @Column(name = "completed_tasks")
    private Integer completedTasks;

    @Column(name = "in_progress_tasks")
    private Integer inProgressTasks;

    @Column(name = "todo_tasks")
    private Integer todoTasks;

    @Column(name = "testing_tasks")
    private Integer testingTasks;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "bugs_fixed")
    private Integer bugsFixed;

    @Transient
    private Double estimatedHours;

    @Transient
    private Double actualHours;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Transient
    private String burndownDataJson;
}
