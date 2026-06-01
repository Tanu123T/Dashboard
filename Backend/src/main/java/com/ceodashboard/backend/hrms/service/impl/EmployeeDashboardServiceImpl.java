package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.exception.ResourceNotFoundException;
import com.ceodashboard.backend.hrms.repository.EmployeeDashboardRepository;
import com.ceodashboard.backend.hrms.service.EmployeeDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Assembles all nine Employee Dashboard sections from the HRMS MySQL DB
 * using pure JDBC queries — no JPA / Hibernate involved.
 *
 * Each section call is individually guarded inside the repository so a
 * missing table or empty result set never brings down the whole dashboard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeDashboardServiceImpl implements EmployeeDashboardService {

    private final EmployeeDashboardRepository dashboardRepository;

    @Override
    @Cacheable(value = "employeeDashboard", key = "#employeeId")
    public EmployeeDashboardResponse getDashboard(Long employeeId) {

        log.info("Building dashboard for employeeId={}", employeeId);

        // Guard: fail fast if the employee row does not exist
        if (!dashboardRepository.employeeExists(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        // ── Section 1: Profile Header ────────────────────────────────────────
        ProfileHeaderDTO profileHeader = dashboardRepository
                .findProfileHeader(employeeId)
                .orElseGet(() -> {
                    log.warn("No profile header found for employeeId={}", employeeId);
                    return ProfileHeaderDTO.builder().employeeId(employeeId).build();
                });

        // ── Section 2: Personal Information ─────────────────────────────────
        PersonalInfoDTO personalInfo = dashboardRepository
                .findPersonalInfo(employeeId)
                .orElseGet(() -> {
                    log.warn("No personal info found for employeeId={}", employeeId);
                    return PersonalInfoDTO.builder().build();
                });

        // ── Section 3: Attendance Analytics (last 30 days) ──────────────────
        AttendanceAnalyticsDTO attendanceAnalytics = dashboardRepository
                .findAttendanceAnalytics(employeeId)
                .orElseGet(() -> {
                    log.warn("No attendance records for employeeId={}", employeeId);
                    return AttendanceAnalyticsDTO.builder()
                            .totalDays(0).presentDays(0).absentDays(0)
                            .attendancePercentage(0.0)
                            .build();
                });

        // ── Section 4: Performance Trends ────────────────────────────────────
        List<PerformanceTrendDTO> performanceTrends =
                dashboardRepository.findPerformanceTrends(employeeId);

        // ── Section 5: Skills ─────────────────────────────────────────────────
        List<SkillDTO> skills = dashboardRepository.findSkills(employeeId);

        // ── Section 6: Projects ───────────────────────────────────────────────
        List<EmployeeProjectDTO> projects = dashboardRepository.findProjects(employeeId);

        // ── Section 7: Education ──────────────────────────────────────────────
        List<EducationDTO> education = dashboardRepository.findEducation(employeeId);

        // ── Section 8: Achievements & Certifications ─────────────────────────
        List<AchievementDTO>    achievements   = dashboardRepository.findAchievements(employeeId);
        List<CertificationDTO>  certifications = dashboardRepository.findCertifications(employeeId);

        // ── Section 9: Work Experience ────────────────────────────────────────
        List<WorkExperienceDTO> workExperience = dashboardRepository.findWorkExperience(employeeId);

        log.info("Dashboard assembled for employeeId={}: attendance={} days, " +
                         "skills={}, projects={}, education={}, workExp={}, " +
                         "achievements={}, certs={}",
                employeeId,
                attendanceAnalytics.getTotalDays(),
                skills.size(), projects.size(), education.size(),
                workExperience.size(), achievements.size(), certifications.size());

        return EmployeeDashboardResponse.builder()
                .profileHeader(profileHeader)
                .personalInfo(personalInfo)
                .attendanceAnalytics(attendanceAnalytics)
                .performanceTrends(performanceTrends)
                .skills(skills)
                .projects(projects)
                .education(education)
                .achievements(achievements)
                .certifications(certifications)
                .workExperience(workExperience)
                .build();
    }
}
