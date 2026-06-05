package com.ceodashboard.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "project_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSchedule {

    @Id
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "project_start_date")
    private LocalDate projectStartDate;

    @Column(name = "project_end_date")
    private LocalDate projectEndDate;

    @Column(name = "project_deadline")
    private LocalDate projectDeadline;

    @Column(name = "requirement_phase_start")
    private LocalDate requirementPhaseStart;

    @Column(name = "requirement_phase_end")
    private LocalDate requirementPhaseEnd;

    @Column(name = "development_phase_start")
    private LocalDate developmentPhaseStart;

    @Column(name = "development_phase_end")
    private LocalDate developmentPhaseEnd;

    @Column(name = "release_phase_start")
    private LocalDate releasePhaseStart;

    @Column(name = "release_phase_end")
    private LocalDate releasePhaseEnd;

    @Column(name = "sprint_duration")
    private Integer sprintDuration;

    @Column(name = "total_no_of_sprints")
    private Integer totalNoOfSprints;
}
