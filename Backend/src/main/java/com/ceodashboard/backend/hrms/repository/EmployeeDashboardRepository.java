package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.entity.Employee;
import com.ceodashboard.backend.hrms.entity.EmployeeProject;
import com.ceodashboard.backend.hrms.entity.ProjectTechnology;
import com.ceodashboard.backend.hrms.entity.WorkExperience;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * HRMS dashboard repository implemented with JPA / Hibernate.
 */
@Repository
@ConditionalOnProperty(prefix = "hrms.db", name = "enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class EmployeeDashboardRepository {

    private final EmployeeRepository employeeRepository;
    private final TechvgUserRepository techvgUserRepository;
    private final ContactRepository contactRepository;
    private final EmployeeLeaveAccountRepository employeeLeaveAccountRepository;
    private final AttendanceRepository attendanceRepository;
    private final AppraisalEvaluationRepository appraisalEvaluationRepository;
    private final AppraisalReviewRepository appraisalReviewRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final EmployeeProjectRepository employeeProjectRepository;
    private final ProjectTechnologyRepository projectTechnologyRepository;
    private final EducationRepository educationRepository;
    private final EmployeeAchievementRepository employeeAchievementRepository;
    private final EmployeeCertificationRepository employeeCertificationRepository;
    private final WorkExperienceRepository workExperienceRepository;

    public List<EmployeeHubItemDTO> findHubEmployees(int limit, int offset) {
        int page = offset / Math.max(limit, 1);
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "id"));
        return employeeRepository.findAllWithDetails(pageable)
                .map(this::toEmployeeHubItem)
                .getContent();
    }

    public long countHubEmployees() {
        return employeeRepository.count();
    }

    public Optional<ProfileHeaderDTO> findProfileHeader(Long employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId)
                .map(employee -> ProfileHeaderDTO.builder()
                        .employeeId(employee.getId())
                        .fullName(buildFullName(employee.getFirstName(), null, employee.getLastName(), employee.getEmployeeCode()))
                        .designation(employee.getDesignation() != null ? employee.getDesignation().getTitle() : null)
                        .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                        .profileImage(techvgUserRepository.findFirstByEmployeeIdOrderByIdDesc(employeeId)
                                .map(u -> u.getImageUrl()).orElse(null))
                        .officialEmail(employee.getOfficialEmail())
                        .build());
    }

    public Optional<PersonalInfoDTO> findPersonalInfo(Long employeeId) {
        return employeeRepository.findByIdWithDetails(employeeId)
                .map(employee -> {
                    String branch = employee.getBranch() != null ? employee.getBranch().getBranchName() : null;
                    String region = employee.getBranch() != null && employee.getBranch().getRegion() != null
                            ? employee.getBranch().getRegion().getRegionName() : null;
                    String phone = contactRepository.findFirstByRefTableIdOrderByIdDesc(employeeId)
                            .map(c -> c.getContact()).orElse(null);
                    String managerName = employee.getReportingManager() != null
                            ? buildFullName(employee.getReportingManager().getFirstName(), null,
                                    employee.getReportingManager().getLastName(), null)
                            : null;
                    LocalDate joinDate = employee.getJoiningDate();
                    int expYears = 0;
                    int expMonths = 0;
                    if (joinDate != null) {
                        Period period = Period.between(joinDate, LocalDate.now());
                        expYears = Math.max(0, period.getYears());
                        expMonths = Math.max(0, period.getMonths());
                    }
                    return PersonalInfoDTO.builder()
                            .officialEmail(employee.getOfficialEmail())
                            .phoneNumber(phone)
                            .branch(branch)
                            .region(region)
                            .joinDate(joinDate)
                            .experienceYears(expYears)
                            .experienceMonths(expMonths)
                            .totalExperience(expYears + "y " + expMonths + "m")
                            .reportingManagerName(managerName)
                            .build();
                });
    }

    public Optional<AttendanceAnalyticsDTO> findAttendanceAnalytics(Long employeeId) {
        LocalDate startDate = LocalDate.now().minusDays(30);
        int totalDays = (int) attendanceRepository.countByEmployeeIdAndAttendanceDateBetween(employeeId, startDate, LocalDate.now());
        int presentDays = (int) attendanceRepository.countByEmployeeIdAndAttendanceDateBetweenAndHasCheckedInTrue(employeeId, startDate, LocalDate.now());
        int absentDays = (int) attendanceRepository.countByEmployeeIdAndAttendanceDateBetweenAndStatus(employeeId, "ABSENT", startDate, LocalDate.now());
        double attendancePercentage = totalDays == 0 ? 0.0 : Math.round((presentDays * 100.0 / totalDays) * 100.0) / 100.0;
        return Optional.of(AttendanceAnalyticsDTO.builder()
                .totalDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(attendancePercentage)
                .build());
    }

    public List<PerformanceTrendDTO> findPerformanceTrends(Long employeeId) {
        return performanceReviewRepository.findPerformanceTrendsByEmployeeId(employeeId);
    }

    public List<SkillDTO> findSkills(Long employeeId) {
        return employeeSkillRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(skill -> SkillDTO.builder()
                        .id(skill.getId())
                        .skillName(skill.getSkillName())
                        .skillLevel(skill.getSkillLevel())
                        .build())
                .collect(Collectors.toList());
    }

    public List<EmployeeProjectDTO> findProjects(Long employeeId) {
        return employeeProjectRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(project -> {
                    List<String> technologies = projectTechnologyRepository.findByProjectId(project.getId())
                            .stream()
                            .map(ProjectTechnology::getTechnologyName)
                            .collect(Collectors.toList());
                    return EmployeeProjectDTO.builder()
                            .id(project.getId())
                            .projectName(project.getProjectName())
                            .projectStatus(project.getProjectStatus())
                            .projectDescription(project.getProjectDescription())
                            .startDate(project.getStartDate())
                            .endDate(project.getEndDate())
                            .technologies(technologies)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<EducationDTO> findEducation(Long employeeId) {
        return educationRepository.findByEmployeeIdOrderByStartYearDesc(employeeId)
                .stream()
                .map(edu -> EducationDTO.builder()
                        .id(edu.getId())
                        .educationType(edu.getEducationType())
                        .subject(edu.getSubject())
                        .institution(edu.getInstitution())
                        .startYear(edu.getStartYear() != null ? edu.getStartYear().toString() : null)
                        .endDate(edu.getEndDate() != null ? edu.getEndDate().toString() : null)
                        .grade(edu.getGrade())
                        .description(edu.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public List<AchievementDTO> findAchievements(Long employeeId) {
        return employeeAchievementRepository.findByEmployeeIdOrderByAchievementDateDesc(employeeId)
                .stream()
                .map(a -> AchievementDTO.builder()
                        .id(a.getId())
                        .title(a.getAchievementTitle())
                        .description(a.getAchievementDescription())
                        .achievementDate(a.getAchievementDate())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CertificationDTO> findCertifications(Long employeeId) {
        return employeeCertificationRepository.findByEmployeeIdOrderByCertificationDateDesc(employeeId)
                .stream()
                .map(c -> CertificationDTO.builder()
                        .id(c.getId())
                        .certificationName(c.getCertificationName())
                        .issuingOrganization(c.getIssuingOrganization())
                        .certificationDate(c.getCertificationDate())
                        .build())
                .collect(Collectors.toList());
    }

    public List<WorkExperienceDTO> findWorkExperience(Long employeeId) {
        return workExperienceRepository.findByEmployeeIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(w -> WorkExperienceDTO.builder()
                        .id(w.getId())
                        .companyName(w.getCompanyName())
                        .jobTitle(w.getJobTitle())
                        .startDate(w.getStartDate())
                        .endDate(w.getEndDate())
                        .description(w.getJobDesc())
                        .status(w.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public boolean employeeExists(Long employeeId) {
        return employeeRepository.existsById(employeeId);
    }

    private String buildFullName(String firstName, String middleName, String lastName, String fallbackCode) {
        String fullName = Stream.of(firstName, middleName, lastName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
        return fullName.isBlank() ? fallbackCode : fullName;
    }

    private EmployeeHubItemDTO toEmployeeHubItem(Employee employee) {
        return EmployeeHubItemDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(buildFullName(employee.getFirstName(), null, employee.getLastName(), employee.getEmployeeCode()))
                .designation(employee.getDesignation() != null ? employee.getDesignation().getTitle() : null)
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .branchName(employee.getBranch() != null ? employee.getBranch().getBranchName() : null)
                .regionName(employee.getBranch() != null && employee.getBranch().getRegion() != null
                        ? employee.getBranch().getRegion().getRegionName() : null)
                .status(employee.getEmployeeStatus())
                .profileImage(techvgUserRepository.findFirstByEmployeeIdOrderByIdDesc(employee.getId())
                        .map(u -> u.getImageUrl()).orElse(null))
                .experienceYears(employee.getJoiningDate() != null
                        ? Period.between(employee.getJoiningDate(), LocalDate.now()).getYears()
                        : 0)
                .build();
    }
}
