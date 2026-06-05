package com.ceodashboard.backend.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDashboardResponse {
    private ProfileHeaderDTO       profileHeader;
    private PersonalInfoDTO        personalInfo;
    private AttendanceAnalyticsDTO attendanceAnalytics;
    private List<PerformanceTrendDTO>  performanceTrends;
    private List<SkillDTO>             skills;
    private List<EmployeeProjectDTO>   projects;
    private List<EducationDTO>         education;
    private List<AchievementDTO>       achievements;
    private List<CertificationDTO>     certifications;
    private List<WorkExperienceDTO>    workExperience;
}
