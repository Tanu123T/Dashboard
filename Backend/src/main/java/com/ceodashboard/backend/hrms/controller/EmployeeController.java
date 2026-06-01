package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.entity.Employee;
import com.ceodashboard.backend.hrms.repository.EmployeeRepository;
import com.ceodashboard.backend.hrms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hrms/employees")
public class EmployeeController {

    private final EmployeeService    employeeService;
    private final EmployeeRepository employeeRepository;
    private final JdbcTemplate       jdbcTemplate;

    /** Active HRMS company — applied to every JDBC query in this controller. */
    @Value("${hrms.company.id:1}")
    private long companyId;

    public EmployeeController(EmployeeService employeeService,
                              EmployeeRepository employeeRepository,
                              @Qualifier("hrmsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.employeeService    = employeeService;
        this.employeeRepository = employeeRepository;
        this.jdbcTemplate       = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(id)));
    }

    @GetMapping("/{identifier}/full")
    public ResponseEntity<ApiResponse<Object>> getEmployeeFull(@PathVariable String identifier) {
        try {
            Employee employee = resolveEmployee(identifier);
            if (employee == null)
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "Employee not found", "No employee with id/code: " + identifier));

            Long empId = employee.getId();

            // ── Attendance (last 30 days, company-filtered) ─────────────────
            Integer totalDays   = safe("SELECT COUNT(*) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY)", Integer.class, empId, companyId);
            Integer presentDays = safe("SELECT COUNT(*) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) AND has_checked_in=1", Integer.class, empId, companyId);
            if (totalDays   == null) totalDays   = 0;
            if (presentDays == null) presentDays = 0;
            int attendanceRate = totalDays == 0 ? 0 : (int) Math.round(presentDays * 100.0 / totalDays);

            Integer absentDays  = safe("SELECT COUNT(*) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) AND status='ABSENT'", Integer.class, empId, companyId);
            Integer leaveDays   = safe("SELECT COUNT(*) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) AND status IN('LEAVE','ON_LEAVE')", Integer.class, empId, companyId);
            Integer lateArriv   = safe("SELECT COUNT(*) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) AND has_checked_in=1 AND hours IS NOT NULL AND hours<>'' AND TIME_TO_SEC(hours)>TIME_TO_SEC('09:30:00')", Integer.class, empId, companyId);
            Double  avgHours    = safe("SELECT AVG(TIME_TO_SEC(hours))/3600 FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 30 DAY) AND hours IS NOT NULL AND hours<>''", Double.class, empId, companyId);
            String  availStatus = safe("SELECT status FROM attendance WHERE employee_id=? AND company_id=? ORDER BY date DESC LIMIT 1", String.class, empId, companyId);
            Integer checkedIn   = safe("SELECT has_checked_in FROM attendance WHERE employee_id=? AND company_id=? ORDER BY date DESC LIMIT 1", Integer.class, empId, companyId);

            // ── Contacts ─────────────────────────────────────────────────────
            String phone = safe("SELECT contact FROM contacts WHERE ref_table_id=? AND company_id=? ORDER BY id DESC LIMIT 1", String.class, empId, companyId);

            // ── Branch / Region ───────────────────────────────────────────────
            Map<String, Object> branchRow = employee.getBranchId() == null ? Map.of()
                : safeRow("SELECT b.branch_name, r.region_name FROM branch b LEFT JOIN region r ON b.region_id=r.id WHERE b.id=? AND b.company_id=?", employee.getBranchId(), companyId);
            String branchName = str(branchRow.get("branch_name"));
            String regionName = str(branchRow.get("region_name"));
            String location   = branchName != null ? branchName : (employee.getBranchId() != null ? "Branch " + employee.getBranchId() : null);

            // ── Leave ─────────────────────────────────────────────────────────
            Double leaveBalance    = safe("SELECT balance FROM employee_leave_account WHERE employee_id=? AND company_id=? ORDER BY id DESC LIMIT 1", Double.class, empId, companyId);
            Double creditedLeaves  = safe("SELECT credited_leaves FROM employee_leave_account WHERE employee_id=? AND company_id=? ORDER BY id DESC LIMIT 1", Double.class, empId, companyId);

            // ── Performance ───────────────────────────────────────────────────
            Double perfScore   = safe("SELECT AVG(CAST(scored_points AS DECIMAL(10,2))) FROM appraisal_evaluation WHERE employee_id=? AND company_id=?", Double.class, empId, companyId);
            Double prodScore   = safe("SELECT ROUND(COUNT(CASE WHEN status='PRESENT' THEN 1 END)*100.0/NULLIF(COUNT(*),0),2) FROM attendance WHERE employee_id=? AND company_id=? AND DATE(date)>=DATE_SUB(CURDATE(),INTERVAL 90 DAY)", Double.class, empId, companyId);
            String apprRating  = safe("SELECT appraisal_status FROM appraisal_review WHERE employee_id=? AND company_id=? ORDER BY id DESC LIMIT 1", String.class, empId, companyId);

            // ── Profile image ─────────────────────────────────────────────────
            Map<String, Object> userRow = safeRow("SELECT image_url, last_modified_date, activated, created_date FROM techvg_user WHERE employee_id=? AND company_id=? LIMIT 1", empId, companyId);
            String profileImage     = str(userRow.get("image_url"));
            String lastLogin        = str(userRow.get("last_modified_date"));
            String accountStatus    = userRow.containsKey("activated") ? String.valueOf(userRow.get("activated")) : null;
            String accountCreatedAt = str(userRow.get("created_date"));

            // ── Performance trends ────────────────────────────────────────────
            List<Map<String, Object>> perfRows = safeList(
                "SELECT DATE_FORMAT(pr.appraisal_date,'%b %Y') AS month, AVG(COALESCE(pr.target_achived,0)) AS avg_value " +
                "FROM performance_review pr INNER JOIN appraisal_review ar ON ar.id=pr.appraisal_review_id " +
                "WHERE ar.employee_id=? AND ar.company_id=? " +
                "GROUP BY DATE_FORMAT(pr.appraisal_date,'%Y-%m') ORDER BY MIN(pr.appraisal_date) DESC LIMIT 6",
                empId, companyId);

            // ── Education ─────────────────────────────────────────────────────
            List<Map<String, Object>> educations = safeList(
                "SELECT id, education_type, institution, DATE_FORMAT(start_year,'%Y') AS start_year, " +
                "DATE_FORMAT(end_date,'%Y') AS end_date, grade, description " +
                "FROM education WHERE employee_id=? AND company_id=? ORDER BY start_year DESC LIMIT 6",
                empId, companyId);

            // ── Work experience ───────────────────────────────────────────────
            List<Map<String, Object>> experiences = safeList(
                "SELECT id, company_name, job_title AS title, DATE_FORMAT(start_date,'%Y-%m-%d') AS start_date, " +
                "DATE_FORMAT(end_date,'%Y-%m-%d') AS end_date, job_desc AS description " +
                "FROM work_experience WHERE employee_id=? AND company_id=? ORDER BY start_date DESC LIMIT 6",
                empId, companyId);

            // ── Build response ────────────────────────────────────────────────
            String fn = ((employee.getFirstName() == null ? "" : employee.getFirstName()) +
                         " " + (employee.getLastName() == null ? "" : employee.getLastName())).trim();

            Map<String, Object> empPayload = new LinkedHashMap<>();
            empPayload.put("id",                   employee.getId());
            empPayload.put("employeeCode",          employee.getEmployeeCode());
            empPayload.put("fullName",              fn.isEmpty() ? employee.getEmployeeCode() : fn);
            empPayload.put("firstName",             employee.getFirstName());
            empPayload.put("lastName",              employee.getLastName());
            empPayload.put("officialEmail",         employee.getOfficialEmail());
            empPayload.put("profileImage",          profileImage);
            empPayload.put("designation",           employee.getDesignation() != null ? employee.getDesignation().getTitle() : null);
            empPayload.put("department",            employee.getDepartment()  != null ? employee.getDepartment().getName()  : null);
            empPayload.put("employmentDate",        employee.getJoiningDate() != null ? employee.getJoiningDate().toString() : null);
            empPayload.put("employeeStatus",        employee.getEmployeeStatus());
            empPayload.put("reportingManagerName",  employee.getReportingManager() != null ? (employee.getReportingManager().getFirstName() + " " + employee.getReportingManager().getLastName()).trim() : null);
            empPayload.put("location",              location);
            empPayload.put("region",                regionName);
            empPayload.put("phoneNumber",           phone);
            empPayload.put("totalExperience",       formatExperience(employee.getJoiningDate()));
            empPayload.put("lastLoginTime",         lastLogin);
            empPayload.put("accountStatus",         accountStatus);

            List<Map<String, Object>> workPayload = new ArrayList<>();
            for (Map<String, Object> exp : experiences) {
                workPayload.add(of("title", exp.get("title"), "companyName", exp.get("company_name"),
                                   "startDate", exp.get("start_date"), "endDate", exp.get("end_date"),
                                   "description", exp.get("description")));
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("employee",         empPayload);
            resp.put("attendance",       of("totalDays", totalDays, "presentDays", presentDays,
                                            "absentDays", absentDays != null ? absentDays : Math.max(0, totalDays - presentDays),
                                            "leaveDays", leaveDays, "lateArrivals", lateArriv,
                                            "averageWorkHours", avgHours, "availabilityStatus", availStatus,
                                            "currentActivity", checkedIn == null ? null : (checkedIn == 1 ? "Checked In" : "Not Checked In"),
                                            "attendanceRate", attendanceRate));
            resp.put("performanceTrends", perfRows);
            resp.put("education",         educations);
            resp.put("workExperience",    workPayload);
            resp.put("leaveAccount",      of("balance", leaveBalance, "creditedLeaves", creditedLeaves));
            resp.put("performance",       of("performanceScore", perfScore, "productivityScore", prodScore, "appraisalRating", apprRating));

            return ResponseEntity.ok(ApiResponse.success(resp));

        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Failed to build employee dashboard",
                                            ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Employee resolveEmployee(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;
        try {
            return employeeRepository.findByIdWithDetails(Long.valueOf(identifier))
                    .orElseGet(() -> employeeRepository.findByEmployeeCode(identifier).orElse(null));
        } catch (NumberFormatException ex) {
            return employeeRepository.findByEmployeeCode(identifier).orElse(null);
        }
    }

    private <T> T safe(String sql, Class<T> type, Object... args) {
        try { return jdbcTemplate.queryForObject(sql, type, args); }
        catch (Exception ex) { return null; }
    }

    private List<Map<String, Object>> safeList(String sql, Object... args) {
        try { return jdbcTemplate.queryForList(sql, args); }
        catch (Exception ex) { return List.of(); }
    }

    private Map<String, Object> safeRow(String sql, Object... args) {
        try { return jdbcTemplate.queryForMap(sql, args); }
        catch (Exception ex) { return new LinkedHashMap<>(); }
    }

    private String str(Object v) { return v == null ? null : String.valueOf(v); }

    private Map<String, Object> of(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private String formatExperience(LocalDate joiningDate) {
        if (joiningDate == null) return null;
        Period p = Period.between(joiningDate, LocalDate.now());
        return Math.max(0, p.getYears()) + "y " + Math.max(0, p.getMonths()) + "m";
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
