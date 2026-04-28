package com.ceodashboard.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    private Long id;

    private Long sprintId;

    private String name;

    private String role;

    private Integer assignedTasks;

    private Integer completedTasks;

    private Integer inProgressTasks;

    private Integer todoTasks;

    private Double estimatedHours;

    private Double actualHours;
}
