package com.ceodashboard.backend.hrms.repository;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pure JDBC repository — company_id filter applied to EVERY query.
 * The active company is driven by hrms.company.id (default 1).
 */
@Repository
@Slf4j
public class EmployeeDashboardRepository {

    private final JdbcTemplate jdbc;

    /** Active HRMS company — filters every single query. */
    @Value("${hrms.company.id:1}")
    private long companyId;

    private final AttendanceSummaryRowMapper  attendanceSummaryRowMapper;
    private final PerformanceTrendRowMapper   performanceTrendRowMapper;
    private final SkillRowMapper              skillRowMapper;
    private final EmployeeProjectRowMapper    employeeProjectRowMapper;
    private final EducationRowMapper          educationRowMapper;
    private final AchievementRowMapper        achievementRowMapper;
    private final CertificationRowMapper      certificationRowMapper;
    private final WorkExperienceRowMapper     workExperienceRowMapper;
    private final EmployeeHubItemRowMapper    employeeHubItemRowMapper;

    public EmployeeDashboardRepository(
            @Qualifier("hrmsJdbcTemplate") JdbcTemplate jdbc,
            AttendanceSummaryRowMapper  attendanceSummaryRowMapper,
            PerformanceTrendRowMapper   performanceTrendRowMapper,
            SkillRowMapper              skillRowMapper,
            EmployeeProjectRowMapper    employeeProjectRowMapper,
            EducationRowMapper          educationRowMapper,
            AchievementRowMapper        achievementRowMapper,
            CertificationRowMapper      certificationRowMapper,
            WorkExperienceRowMapper     workExperienceRowMapper,
            EmployeeHubItemRowMapper    employeeHubItemRowMapper) {

        this.jdbc                      = jdbc;
        this.attendanceSummaryRowMapper = attendanceSummaryRowMapper;
        this.performanceTrendRowMapper  = performanceTrendRowMapper;
        this.skillRowMapper             = skillRowMapper;
        this.employeeProjectRowMapper   = employeeProjectRowMapper;
        this.educationRowMapper         = educationRowMapper;
        this.achievementRowMapper       = achievementRowMapper;
        this.certificationRowMapper     = certificationRowMapper;
        this.workExperienceRowMapper    = workExperienceRowMapper;
        this.employeeHubItemRowMapper   = employeeHubItemRowMapper;
    }

    // =========================================================================
    // SAFE HELPERS
    // =========================================================================

    private Map<String, Object> safeRow(String sql, Object... args) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
            return rows.isEmpty() ? Collections.emptyMap() : rows.get(0);
        } catch (DataAccessException ex) {
            log.warn("safeRow: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private String safeStr(String sql, Object... args) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, args);
            if (rows.isEmpty()) return null;
            Object v = rows.get(0).values().iterator().next();
            return v == null ? null : String.valueOf(v).trim();
        } catch (DataAccessException ex) {
            log.warn("safeStr: {}", ex.getMessage());
            return null;
        }
    }

    private <T> List<T> safeList(String sql, org.springframework.jdbc.core.RowMapper<T> mapper,
                                  Object... args) {
        try {
            return jdbc.query(sql, mapper, args);
        } catch (DataAccessException ex) {
            log.warn("safeList: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private Long lng(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); }
        catch (NumberFormatException e) { return null; }
    }

    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof Date)      return ((Date) v).toLocalDate();
        if (v instanceof Timestamp) return ((Timestamp) v).toLocalDateTime().toLocalDate();
        if (v instanceof LocalDate) return (LocalDate) v;
        try { return LocalDate.parse(String.valueOf(v).substring(0, 10)); }
        catch (Exception e) { return null; }
    }

    // =========================================================================
    // HUB LIST  — company_id filter applied to employee and all joined tables
    // =========================================================================
    public List<EmployeeHubItemDTO> findHubEmployees(int limit, int offset) {
        String sql =
            "SELECT e.id, e.emp_unique_id AS employee_code, " +
            "TRIM(CONCAT_WS(' ', NULLIF(TRIM(e.first_name),''), NULLIF(TRIM(e.middle_name),''), NULLIF(TRIM(e.last_name),''))) AS full_name, " +
            "des.name AS designation, dept.name AS department, b.branch_name, r.region_name, " +
            "e.status, tu.image_url AS profile_image, " +
            "COALESCE(TIMESTAMPDIFF(YEAR, e.joindate, CURDATE()), 0) AS experience_years " +
            "FROM employee e " +
            "LEFT JOIN designation des  ON des.id  = e.designation_id  AND des.company_id  = ? " +
            "LEFT JOIN department  dept ON dept.id = e.department_id   AND dept.company_id = ? " +
            "LEFT JOIN branch      b    ON b.id    = e.branch_id       AND b.company_id    = ? " +
            "LEFT JOIN region      r    ON r.id    = e.region_id       AND r.company_id    = ? " +
            "LEFT JOIN techvg_user tu   ON tu.employee_id = e.id       AND tu.company_id   = ? " +
            "WHERE e.company_id = ? " +
            "ORDER BY e.id LIMIT ? OFFSET ?";
        return safeList(sql, employeeHubItemRowMapper,
                companyId, companyId, companyId, companyId, companyId, companyId, limit, offset);
    }

    public long countHubEmployees() {
        try {
            Long c = jdbc.queryForObject("SELECT COUNT(*) FROM employee WHERE company_id = ?",
                                         Long.class, companyId);
            return c != null ? c : 0L;
        } catch (DataAccessException ex) {
            log.error("countHubEmployees: {}", ex.getMessage());
            return 0L;
        }
    }

    // =========================================================================
    // 1. PROFILE HEADER — individual queries, each filtered by company_id
    // =========================================================================
    public Optional<ProfileHeaderDTO> findProfileHeader(Long employeeId) {
        Map<String, Object> emp = safeRow(
            "SELECT first_name, middle_name, last_name, email_id, designation_id, department_id " +
            "FROM employee WHERE id = ? AND company_id = ?", employeeId, companyId);

        if (emp.isEmpty()) {
            log.warn("findProfileHeader: no row for employeeId={} companyId={}", employeeId, companyId);
            return Optional.empty();
        }

        String fullName = Stream.of(str(emp, "first_name"), str(emp, "middle_name"), str(emp, "last_name"))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));

        String designation = null;
        Long desigId = lng(emp, "designation_id");
        if (desigId != null)
            designation = safeStr("SELECT name FROM designation WHERE id = ? AND company_id = ?",
                                  desigId, companyId);

        String department = null;
        Long deptId = lng(emp, "department_id");
        if (deptId != null)
            department = safeStr("SELECT name FROM department WHERE id = ? AND company_id = ?",
                                 deptId, companyId);

        String profileImage = safeStr(
            "SELECT image_url FROM techvg_user WHERE employee_id = ? AND company_id = ? LIMIT 1",
            employeeId, companyId);

        log.debug("findProfileHeader: id={} name='{}' desig='{}' dept='{}'",
                  employeeId, fullName, designation, department);

        return Optional.of(ProfileHeaderDTO.builder()
                .employeeId(employeeId)
                .fullName(fullName.isBlank() ? null : fullName)
                .designation(designation)
                .department(department)
                .profileImage(profileImage)
                .officialEmail(str(emp, "email_id"))
                .build());
    }

    // =========================================================================
    // 2. PERSONAL INFORMATION — all sub-queries filtered by company_id
    // =========================================================================
    public Optional<PersonalInfoDTO> findPersonalInfo(Long employeeId) {
        Map<String, Object> emp = safeRow(
            "SELECT email_id, joindate, branch_id, region_id, reporting_emp_id " +
            "FROM employee WHERE id = ? AND company_id = ?", employeeId, companyId);

        if (emp.isEmpty()) {
            log.warn("findPersonalInfo: no row for employeeId={} companyId={}", employeeId, companyId);
            return Optional.empty();
        }

        String branch = null;
        Long branchId = lng(emp, "branch_id");
        if (branchId != null)
            branch = safeStr("SELECT branch_name FROM branch WHERE id = ? AND company_id = ?",
                             branchId, companyId);

        String region = null;
        Long regionId = lng(emp, "region_id");
        if (regionId != null)
            region = safeStr("SELECT region_name FROM region WHERE id = ? AND company_id = ?",
                             regionId, companyId);

        String phone = safeStr(
            "SELECT contact FROM contacts WHERE ref_table_id = ? AND company_id = ? ORDER BY id DESC LIMIT 1",
            employeeId, companyId);

        String managerName = null;
        Long mgrId = lng(emp, "reporting_emp_id");
        if (mgrId != null) {
            Map<String, Object> mgr = safeRow(
                "SELECT first_name, last_name FROM employee WHERE id = ? AND company_id = ?",
                mgrId, companyId);
            if (!mgr.isEmpty()) {
                managerName = Stream.of(str(mgr, "first_name"), str(mgr, "last_name"))
                        .filter(s -> s != null && !s.isBlank())
                        .collect(Collectors.joining(" "));
            }
        }

        LocalDate joinDate = toLocalDate(emp.get("joindate"));
        int expYears = 0, expMonths = 0;
        if (joinDate != null) {
            Period p = Period.between(joinDate, LocalDate.now());
            expYears  = Math.max(0, p.getYears());
            expMonths = Math.max(0, p.getMonths());
        }

        return Optional.of(PersonalInfoDTO.builder()
                .officialEmail(str(emp, "email_id"))
                .phoneNumber(phone)
                .branch(branch)
                .region(region)
                .joinDate(joinDate)
                .experienceYears(expYears)
                .experienceMonths(expMonths)
                .totalExperience(expYears + "y " + expMonths + "m")
                .reportingManagerName(managerName == null || managerName.isBlank() ? null : managerName)
                .build());
    }

    // =========================================================================
    // 3. ATTENDANCE ANALYTICS  — filtered by employee_id AND company_id
    // =========================================================================
    public Optional<AttendanceAnalyticsDTO> findAttendanceAnalytics(Long employeeId) {
        String sql =
            "SELECT COUNT(*) AS total_days, " +
            "COUNT(CASE WHEN status='PRESENT' THEN 1 END) AS present_days, " +
            "COUNT(CASE WHEN status='ABSENT'  THEN 1 END) AS absent_days, " +
            "ROUND(COUNT(CASE WHEN status='PRESENT' THEN 1 END)*100.0/NULLIF(COUNT(*),0),2) AS attendance_percentage " +
            "FROM attendance " +
            "WHERE employee_id = ? AND company_id = ? AND DATE(date) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        List<AttendanceAnalyticsDTO> rows = safeList(sql, attendanceSummaryRowMapper, employeeId, companyId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    // =========================================================================
    // 4. PERFORMANCE TRENDS — filtered via appraisal_review company_id
    // =========================================================================
    public List<PerformanceTrendDTO> findPerformanceTrends(Long employeeId) {
        String sql =
            "SELECT DATE_FORMAT(pr.appraisal_date,'%b') AS month, " +
            "ROUND(AVG(COALESCE(pr.target_achived,0)),0) AS score, " +
            "DATE_FORMAT(pr.appraisal_date,'%Y-%m') AS month_sort " +
            "FROM performance_review pr " +
            "INNER JOIN appraisal_review ar ON ar.id = pr.appraisal_review_id " +
            "WHERE ar.employee_id = ? AND ar.company_id = ? " +
            "GROUP BY DATE_FORMAT(pr.appraisal_date,'%Y-%m'), DATE_FORMAT(pr.appraisal_date,'%b') " +
            "ORDER BY month_sort ASC LIMIT 12";
        return safeList(sql, performanceTrendRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 5. SKILLS
    // =========================================================================
    public List<SkillDTO> findSkills(Long employeeId) {
        return safeList(
            "SELECT id, skill_name, skill_level FROM employee_skill WHERE employee_id = ? AND company_id = ? ORDER BY created_at DESC",
            skillRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 6. PROJECTS
    // =========================================================================
    public List<EmployeeProjectDTO> findProjects(Long employeeId) {
        String sql =
            "SELECT ep.id, ep.project_name, ep.project_status, ep.project_description, " +
            "ep.start_date, ep.end_date, " +
            "GROUP_CONCAT(pt.technology_name ORDER BY pt.id SEPARATOR ',') AS technologies " +
            "FROM employee_project ep " +
            "LEFT JOIN project_technology pt ON pt.project_id = ep.id " +
            "WHERE ep.employee_id = ? AND ep.company_id = ? " +
            "GROUP BY ep.id, ep.project_name, ep.project_status, ep.project_description, ep.start_date, ep.end_date " +
            "ORDER BY ep.start_date DESC";
        return safeList(sql, employeeProjectRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 7. EDUCATION — filtered by employee_id AND company_id
    // =========================================================================
    public List<EducationDTO> findEducation(Long employeeId) {
        String sql =
            "SELECT id, education_type, subject, institution, " +
            "DATE_FORMAT(start_year,'%Y') AS start_year, " +
            "DATE_FORMAT(end_date,'%Y')   AS end_date, " +
            "grade, description " +
            "FROM education WHERE employee_id = ? AND company_id = ? ORDER BY start_year DESC";
        return safeList(sql, educationRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 8a. ACHIEVEMENTS
    // =========================================================================
    public List<AchievementDTO> findAchievements(Long employeeId) {
        return safeList(
            "SELECT id, achievement_title, achievement_description, achievement_date " +
            "FROM employee_achievement WHERE employee_id = ? AND company_id = ? ORDER BY achievement_date DESC",
            achievementRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 8b. CERTIFICATIONS
    // =========================================================================
    public List<CertificationDTO> findCertifications(Long employeeId) {
        return safeList(
            "SELECT id, certification_name, issuing_organization, certification_date " +
            "FROM employee_certification WHERE employee_id = ? AND company_id = ? ORDER BY certification_date DESC",
            certificationRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // 9. WORK EXPERIENCE — filtered by employee_id AND company_id
    // =========================================================================
    public List<WorkExperienceDTO> findWorkExperience(Long employeeId) {
        String sql =
            "SELECT id, company_name, job_title, " +
            "DATE_FORMAT(start_date,'%Y-%m-%d') AS start_date, " +
            "DATE_FORMAT(end_date,  '%Y-%m-%d') AS end_date, " +
            "job_desc AS description, status " +
            "FROM work_experience WHERE employee_id = ? AND company_id = ? ORDER BY start_date DESC";
        return safeList(sql, workExperienceRowMapper, employeeId, companyId);
    }

    // =========================================================================
    // UTILITY
    // =========================================================================
    public boolean employeeExists(Long employeeId) {
        try {
            Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM employee WHERE id = ? AND company_id = ?",
                Integer.class, employeeId, companyId);
            return c != null && c > 0;
        } catch (DataAccessException ex) {
            log.error("employeeExists failed for id={}: {}", employeeId, ex.getMessage());
            return false;
        }
    }
}
