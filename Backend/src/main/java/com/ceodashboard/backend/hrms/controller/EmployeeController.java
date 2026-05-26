package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.entity.Employee;
import com.ceodashboard.backend.hrms.repository.EmployeeRepository;
import com.ceodashboard.backend.hrms.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hrms/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final JdbcTemplate jdbcTemplate;

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository, @Qualifier("hrmsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(id)));
    }

    /**
     * GET /api/v1/hrms/employees/{id}/full
     * Returns an enriched employee dashboard assembled from multiple HRMS tables.
     */
    @GetMapping("/{identifier}/full")
    public ResponseEntity<ApiResponse<Object>> getEmployeeFull(@PathVariable String identifier) {
        try {
            // Basic employee row
            Employee employee = resolveEmployee(identifier);
            if (employee == null) {
                return ResponseEntity.status(404).body(ApiResponse.error(404, "Employee not found", "No employee exists with id or code " + identifier));
            }

            Long employeeId = employee.getId();

                // Attendance in last 30 days
            String totalDaysSql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            Integer totalDays = jdbcTemplate.queryForObject(totalDaysSql, Integer.class, employeeId);
            if (totalDays == null) totalDays = 0;
            String presentDaysSql = "SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND has_checked_in = 1";
            Integer presentDays = jdbcTemplate.queryForObject(presentDaysSql, Integer.class, employeeId);
            if (presentDays == null) presentDays = 0;
            int attendanceRate = totalDays == 0 ? 0 : (int) Math.round((presentDays * 100.0) / totalDays);

                Integer absentDays = safeQueryValue("SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND status = 'ABSENT'", Integer.class, employeeId);
                Integer leaveDays = safeQueryValue("SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND status IN ('LEAVE','ON_LEAVE')", Integer.class, employeeId);
                Integer lateArrivals = safeQueryValue("SELECT COUNT(*) FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND has_checked_in = 1 AND hours IS NOT NULL AND hours <> '' AND TIME_TO_SEC(hours) > TIME_TO_SEC('09:30:00')", Integer.class, employeeId);
                Double avgWorkHours = safeQueryValue("SELECT AVG(TIME_TO_SEC(hours)) / 3600 FROM attendance WHERE employee_id = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND hours IS NOT NULL AND hours <> ''", Double.class, employeeId);
                String availabilityStatus = safeQueryValue("SELECT status FROM attendance WHERE employee_id = ? ORDER BY date DESC LIMIT 1", String.class, employeeId);
                Integer currentActivityCheckedIn = safeQueryValue("SELECT has_checked_in FROM attendance WHERE employee_id = ? ORDER BY date DESC LIMIT 1", Integer.class, employeeId);

                    // Contact info
                    String phoneNumber = safeQueryValue("SELECT contact FROM contacts WHERE employee_id = ? ORDER BY id DESC LIMIT 1", String.class, employeeId);

                    // Location info
                    Map<String, Object> branchRow = employee.getBranchId() == null
                        ? new LinkedHashMap<>()
                        : safeQueryRow("SELECT b.branch_name, r.region_name FROM branch b LEFT JOIN region r ON b.region_id = r.id WHERE b.id = ?", employee.getBranchId());
                String branchName = asString(branchRow.get("branch_name"));
                String regionName = asString(branchRow.get("region_name"));
                String location = branchName != null ? branchName : (employee.getBranchId() != null ? "Branch " + employee.getBranchId() : null);

                // Leave info
                Double leaveBalance = safeQueryValue("SELECT balance FROM employee_leave_account WHERE employee_id = ? ORDER BY id DESC LIMIT 1", Double.class, employeeId);
                Double creditedLeaves = safeQueryValue("SELECT credited_leaves FROM employee_leave_account WHERE employee_id = ? ORDER BY id DESC LIMIT 1", Double.class, employeeId);

                // Performance and appraisal
                Double performanceScore = safeQueryValue("SELECT AVG(scored_points) FROM appraisal_evaluation WHERE employee_id = ?", Double.class, employeeId);
                Double productivityScore = safeQueryValue("SELECT AVG(scored_points) FROM appraisal_evaluation WHERE employee_id = ?", Double.class, employeeId);
                String appraisalRating = safeQueryValue("SELECT appraisal_status FROM appraisal_review WHERE employee_id = ? ORDER BY id DESC LIMIT 1", String.class, employeeId);

                    // Account details
                    String officialEmail = employee.getOfficialEmail();
                    String employeeCode = employee.getEmployeeCode();
                    Map<String, Object> accountRow = new LinkedHashMap<>();
                    if (officialEmail != null || employeeCode != null) {
                    accountRow = safeQueryRow(
                        "SELECT image_url, last_modified_date, activated, created_date FROM techvg_user WHERE email_id = ? OR email = ? OR login = ? LIMIT 1",
                        officialEmail, officialEmail, employeeCode);
                    if (accountRow.isEmpty()) {
                        accountRow = safeQueryRow(
                            "SELECT image_url, last_modified_date, activated, created_date FROM jhi_user WHERE email = ? OR login = ? LIMIT 1",
                            officialEmail, employeeCode);
                    }
                    }
                String profileImage = asString(accountRow.get("image_url"));
                String lastLoginTime = asString(accountRow.get("last_modified_date"));
                String accountStatus = accountRow.containsKey("activated") ? String.valueOf(accountRow.get("activated")) : null;
                String accountCreatedAt = asString(accountRow.get("created_date"));

                // Role info
                    List<Map<String, Object>> roleRows = (officialEmail == null && employeeCode == null)
                        ? List.of()
                        : safeQueryList(
                            "SELECT r.name AS role_name FROM rel_techvg_user__techvg_role ur "
                                + "JOIN techvg_role r ON ur.techvg_role_id = r.id "
                                + "JOIN techvg_user u ON u.id = ur.techvg_user_id "
                                + "WHERE u.email_id = ? OR u.email = ? OR u.login = ?",
                            officialEmail, officialEmail, employeeCode);
                List<String> roleNames = roleRows.stream()
                    .map(row -> asString(row.get("role_name")))
                    .filter(value -> value != null && !value.isBlank())
                    .distinct()
                    .collect(Collectors.toList());

            // Performance trends (last 6 months) from performance_review
            String perfSql = "SELECT DATE_FORMAT(appraisal_date, '%b %Y') AS month, AVG(COALESCE(target_achived,0)) AS avg_value "
                    + "FROM performance_review WHERE employee_id = ? GROUP BY DATE_FORMAT(appraisal_date, '%Y-%m') ORDER BY MIN(appraisal_date) DESC LIMIT 6";
            List<Map<String, Object>> perfRows = safeQueryList(perfSql, employeeId);

            // Education
            String eduSql = "SELECT id, education_type, institution, start_year, end_date, grade, description FROM education WHERE employee_id = ? ORDER BY start_year DESC LIMIT 6";
            List<Map<String, Object>> educations = safeQueryList(eduSql, employeeId);

            // Work experience
            String workSql = "SELECT id, company_name, job_title AS title, start_date, end_date, description FROM work_experience WHERE employee_id = ? ORDER BY start_date DESC LIMIT 6";
            List<Map<String, Object>> experiences = safeQueryList(workSql, employeeId);

            // Projects via team_members -> sprints -> projects
            String projSql = "SELECT DISTINCT p.id, p.name, p.status, p.progress, p.tech_stack_csv, p.client_name, p.description FROM team_members tm "
                    + "JOIN sprints s ON tm.sprint_id = s.id "
                    + "JOIN projects p ON s.project_id = p.id "
                    + "WHERE tm.name LIKE CONCAT('%', ?, '%') LIMIT 20";
            String fullName = (employee.getFirstName() == null ? "" : employee.getFirstName()) + " " + (employee.getLastName() == null ? "" : employee.getLastName());
            List<Map<String, Object>> projects = safeQueryList(projSql, fullName.trim());

            Set<String> skillSet = new LinkedHashSet<>();
            for (Map<String, Object> project : projects) {
                String techStackCsv = asString(project.get("tech_stack_csv"));
                for (String tech : splitCsv(techStackCsv)) {
                    skillSet.add(tech);
                }
            }

            List<Map<String, Object>> achievements = new java.util.ArrayList<>();
            if (totalDays > 0) {
                achievements.add(mapOf(
                        "title", "Attendance rate " + attendanceRate + "%",
                        "date", "Last 30 days"
                ));
            }
            if (!projects.isEmpty()) {
                achievements.add(mapOf(
                        "title", "Active on " + projects.size() + " project" + (projects.size() == 1 ? "" : "s"),
                        "date", "Current"
                ));
            }
            if (!educations.isEmpty()) {
                achievements.add(mapOf(
                        "title", "Education records available",
                        "date", asString(educations.get(0).get("start_year"))
                ));
            }

            String employmentDate = employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : null;
            String totalExperience = formatExperience(employee.getJoiningDate());
            Double stabilityPercentage = computeStabilityPercentage(employee.getJoiningDate(), employee.getEmployeeStatus());
            String attritionRisk = null;

            Map<String, Object> employeePayload = new LinkedHashMap<>();
            employeePayload.put("id", employee.getId());
            employeePayload.put("employeeCode", employee.getEmployeeCode());
            employeePayload.put("fullName", fullName.trim().isEmpty() ? employee.getEmployeeCode() : fullName.trim());
            employeePayload.put("firstName", employee.getFirstName());
            employeePayload.put("lastName", employee.getLastName());
            employeePayload.put("officialEmail", employee.getOfficialEmail());
            employeePayload.put("profileImage", profileImage);
            employeePayload.put("designation", employee.getDesignation() != null ? employee.getDesignation().getTitle() : null);
            employeePayload.put("department", employee.getDepartment() != null ? employee.getDepartment().getName() : null);
            employeePayload.put("employmentDate", employmentDate);
            employeePayload.put("employmentTypeId", employee.getEmploymentType());
            employeePayload.put("employeeStatus", employee.getEmployeeStatus());
            employeePayload.put("reportingManagerName", employee.getReportingManager() != null ? (employee.getReportingManager().getFirstName() + " " + employee.getReportingManager().getLastName()).trim() : null);
            employeePayload.put("reportingManagerId", employee.getReportingManager() != null ? employee.getReportingManager().getId() : null);
            employeePayload.put("branchId", employee.getBranchId());
            employeePayload.put("companyId", employee.getCompanyId());
            employeePayload.put("location", location);
            employeePayload.put("region", regionName);
            employeePayload.put("phoneNumber", phoneNumber);
            employeePayload.put("totalExperience", totalExperience);
            employeePayload.put("stabilityPercentage", stabilityPercentage);
            employeePayload.put("attritionRisk", attritionRisk);
            employeePayload.put("roleNames", roleNames);
            employeePayload.put("lastLoginTime", lastLoginTime);
            employeePayload.put("accountStatus", accountStatus);
            employeePayload.put("accountCreatedAt", accountCreatedAt);
            employeePayload.put("updatedTimestamp", employee.getUpdatedAt() != null ? employee.getUpdatedAt().toString() : null);

            List<Map<String, Object>> projectPayload = new java.util.ArrayList<>();
            for (Map<String, Object> project : projects) {
                projectPayload.add(mapOf(
                        "id", project.get("id"),
                        "name", project.get("name"),
                        "status", project.get("status"),
                        "progress", project.get("progress"),
                        "client", project.get("client_name"),
                        "description", project.get("description"),
                        "techStack", splitCsv(asString(project.get("tech_stack_csv")))
                ));
            }

            List<Map<String, Object>> workPayload = new java.util.ArrayList<>();
            for (Map<String, Object> exp : experiences) {
                workPayload.add(mapOf(
                        "title", exp.get("title"),
                        "companyName", exp.get("company_name"),
                        "startDate", exp.get("start_date"),
                        "endDate", exp.get("end_date"),
                        "description", exp.get("description")
                ));
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("employee", employeePayload);
                    resp.put("attendance", mapOf(
                        "totalDays", totalDays,
                        "presentDays", presentDays,
                        "absentDays", absentDays == null ? Math.max(0, totalDays - presentDays) : absentDays,
                        "leaveDays", leaveDays,
                        "lateArrivals", lateArrivals,
                        "averageWorkHours", avgWorkHours,
                        "availabilityStatus", availabilityStatus,
                        "currentActivity", currentActivityCheckedIn == null ? null : (currentActivityCheckedIn == 1 ? "Checked In" : "Not Checked In"),
                        "attendanceRate", attendanceRate
                    ));
            resp.put("performanceTrends", perfRows);
            resp.put("education", educations);
            resp.put("workExperience", workPayload);
            resp.put("projects", projectPayload);
            resp.put("skills", new java.util.ArrayList<>(skillSet));
                    resp.put("leaveAccount", mapOf(
                        "balance", leaveBalance,
                        "creditedLeaves", creditedLeaves
                    ));
                    resp.put("performance", mapOf(
                        "performanceScore", performanceScore,
                        "productivityScore", productivityScore,
                        "appraisalRating", appraisalRating
                    ));
                resp.put("roles", roleNames);
            resp.put("achievements", achievements);

                List<Map<String, Object>> detailItems = new ArrayList<>();
                addDetailItem(detailItems, "Employee ID", employee.getId());
                addDetailItem(detailItems, "Employee Code", employee.getEmployeeCode());
                addDetailItem(detailItems, "Full Name", employeePayload.get("fullName"));
                addDetailItem(detailItems, "Profile Image", profileImage);
                addDetailItem(detailItems, "Designation", employeePayload.get("designation"));
                addDetailItem(detailItems, "Department", employeePayload.get("department"));
                addDetailItem(detailItems, "Role", roleNames.isEmpty() ? null : String.join(", ", roleNames));
                addDetailItem(detailItems, "Location", location);
                addDetailItem(detailItems, "Region", regionName);
                addDetailItem(detailItems, "Employment Type", employee.getEmploymentType());
                addDetailItem(detailItems, "Employee Status", employee.getEmployeeStatus());
                addDetailItem(detailItems, "Official Email", employee.getOfficialEmail());
                addDetailItem(detailItems, "Phone Number", phoneNumber);
                addDetailItem(detailItems, "Joining Date", employmentDate);
                addDetailItem(detailItems, "Total Experience", totalExperience);
                addDetailItem(detailItems, "Reporting Manager", employeePayload.get("reportingManagerName"));
                addDetailItem(detailItems, "Attendance %", attendanceRate);
                addDetailItem(detailItems, "Present Days", presentDays);
                addDetailItem(detailItems, "Absent Days", absentDays);
                addDetailItem(detailItems, "Leave Days", leaveDays);
                addDetailItem(detailItems, "Late Arrivals", lateArrivals);
                addDetailItem(detailItems, "Average Work Hours", avgWorkHours);
                addDetailItem(detailItems, "Availability Status", availabilityStatus);
                addDetailItem(detailItems, "Current Activity", currentActivityCheckedIn == null ? null : (currentActivityCheckedIn == 1 ? "Checked In" : "Not Checked In"));
                addDetailItem(detailItems, "Leave Balance", leaveBalance);
                addDetailItem(detailItems, "Performance Score", performanceScore);
                addDetailItem(detailItems, "Productivity Score", productivityScore);
                addDetailItem(detailItems, "Appraisal Rating", appraisalRating);
                addDetailItem(detailItems, "Stability %", stabilityPercentage);
                addDetailItem(detailItems, "Attrition Risk", attritionRisk);
                addDetailItem(detailItems, "Last Login", lastLoginTime);
                addDetailItem(detailItems, "Account Status", accountStatus);
                addDetailItem(detailItems, "Account Created", accountCreatedAt);
                addDetailItem(detailItems, "Updated Timestamp", employeePayload.get("updatedTimestamp"));

                resp.put("detailItems", detailItems);

            return ResponseEntity.ok(ApiResponse.success(resp));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Failed to build employee dashboard", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    private Employee resolveEmployee(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        try {
            Long id = Long.valueOf(identifier);
            return employeeRepository.findByIdWithDetails(id)
                    .orElseGet(() -> employeeRepository.findByEmployeeCode(identifier).orElse(null));
        } catch (NumberFormatException ex) {
            return employeeRepository.findByEmployeeCode(identifier).orElse(null);
        }
    }

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int index = 0; index + 1 < entries.length; index += 2) {
            map.put(String.valueOf(entries[index]), entries[index + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> safeQueryList(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private <T> T safeQueryValue(String sql, Class<T> type, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, type, args);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> safeQueryRow(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForMap(sql, args);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String formatExperience(LocalDate joiningDate) {
        if (joiningDate == null) {
            return null;
        }

        Period period = Period.between(joiningDate, LocalDate.now());
        int years = Math.max(0, period.getYears());
        int months = Math.max(0, period.getMonths());
        return years + "y " + months + "m";
    }

    private Double computeStabilityPercentage(LocalDate joiningDate, String status) {
        if (joiningDate == null) {
            return null;
        }

        Period period = Period.between(joiningDate, LocalDate.now());
        double years = Math.max(0, period.getYears() + (period.getMonths() / 12.0));
        double stability = Math.min(100.0, (years / 5.0) * 100.0);
        return Math.round(stability * 10.0) / 10.0;
    }

    private void addDetailItem(List<Map<String, Object>> items, String label, Object value) {
        items.add(mapOf("label", label, "value", value));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getMyProfile()));
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getMyTeam(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getMyTeam(pageable)));
    }
}
