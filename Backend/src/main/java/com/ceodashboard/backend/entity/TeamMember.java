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

@Entity
@Table(name = "project_team")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "member_first_name", length = 50)
    private String memberFirstName;

    @Column(name = "member_last_name", length = 50)
    private String memberLastName;

    @Column(name = "project_role", length = 100)
    private String role;

    @Transient
    private String name;

    @Transient
    private Integer assignedTasks;

    @Transient
    private Integer completedTasks;

    @Transient
    private Integer inProgressTasks;

    @Transient
    private Integer todoTasks;

    @Transient
    private Double estimatedHours;

    @Transient
    private Double actualHours;

    public String getName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (memberFirstName == null && memberLastName == null) return null;
        if (memberFirstName == null) return memberLastName;
        if (memberLastName == null) return memberFirstName;
        return memberFirstName + " " + memberLastName;
    }
}
