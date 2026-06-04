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
import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "task_id")
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "sprint_id", nullable = false)
    private Long sprintId;

    @Column(name = "sprint_task", length = 500)
    private String title;

    @Column(name = "task_status", length = 50)
    private String status;

    @Column(name = "task_story_points")
    private Integer storyPoints;

    @Column(name = "assigned_member_id")
    private Long assignedMemberId;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "actual_hours")
    private Double actualHours;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "assignee", length = 255)
    private String assignee;

    @Column(name = "task_type", length = 100)
    private String type;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;
}
