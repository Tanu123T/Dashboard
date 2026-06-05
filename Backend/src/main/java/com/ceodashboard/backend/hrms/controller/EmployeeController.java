package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeProfileDTO;
import com.ceodashboard.backend.hrms.entity.Attendance;
import com.ceodashboard.backend.hrms.entity.AppraisalReview;
import com.ceodashboard.backend.hrms.entity.Employee;
import com.ceodashboard.backend.hrms.entity.EmployeeLeaveAccount;
import com.ceodashboard.backend.hrms.entity.TechvgUser;
import com.ceodashboard.backend.hrms.repository.AppraisalEvaluationRepository;
import com.ceodashboard.backend.hrms.repository.AppraisalReviewRepository;
import com.ceodashboard.backend.hrms.repository.AttendanceRepository;
import com.ceodashboard.backend.hrms.repository.ContactRepository;
import com.ceodashboard.backend.hrms.repository.EducationRepository;
import com.ceodashboard.backend.hrms.repository.EmployeeLeaveAccountRepository;
import com.ceodashboard.backend.hrms.repository.EmployeeRepository;
import com.ceodashboard.backend.hrms.repository.PerformanceReviewRepository;
import com.ceodashboard.backend.hrms.repository.TechvgUserRepository;
import com.ceodashboard.backend.hrms.repository.WorkExperienceRepository;
import com.ceodashboard.backend.hrms.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hrms/employees")
@ConditionalOnProperty(prefix = "hrms.db", name = "enabled", havingValue = "true", matchIfMissing = false)
public class EmployeeController {

    private final EmployeeService    employeeService;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ContactRepository contactRepository;
    private final EmployeeLeaveAccountRepository leaveAccountRepository;
    private final AppraisalEvaluationRepository appraisalEvaluationRepository;
    private final AppraisalReviewRepository appraisalReviewRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final TechvgUserRepository techvgUserRepository;

    public EmployeeController(EmployeeService employeeService,
                              EmployeeRepository employeeRepository,
                              AttendanceRepository attendanceRepository,
                              ContactRepository contactRepository,
                              EmployeeLeaveAccountRepository leaveAccountRepository,
                              AppraisalEvaluationRepository appraisalEvaluationRepository,
                              AppraisalReviewRepository appraisalReviewRepository,
                              PerformanceReviewRepository performanceReviewRepository,
                              EducationRepository educationRepository,
                              WorkExperienceRepository workExperienceRepository,
                              TechvgUserRepository techvgUserRepository) {
        this.employeeService               = employeeService;
        this.employeeRepository            = employeeRepository;
        this.attendanceRepository          = attendanceRepository;
        this.contactRepository             = contactRepository;
        this.leaveAccountRepository        = leaveAccountRepository;
        this.appraisalEvaluationRepository = appraisalEvaluationRepository;
        this.appraisalReviewRepository     = appraisalReviewRepository;
        this.performanceReviewRepository   = performanceReviewRepository;
        this.educationRepository           = educationRepository;
        this.workExperienceRepository      = workExperienceRepository;
        this.techvgUserRepository          = techvgUserRepository;
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

            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
            List<Attendance> recentAttendance = attendanceRepository
                    .findByEmployeeIdAndDateRange(empId, thirtyDaysAgo, LocalDate.now());

            int totalDays = recentAttendance.size();
            int presentDays = (int) recentAttendance.stream()
                    .filter(att -> Boolean.TRUE.equals(att.getHasCheckedIn()))
                    .count();
            int attendanceRate = totalDays == 0 ? 0 : (int) Math.round(presentDays * 100.0 / totalDays);

            int absentDays = (int) attendanceRepository
                    .countByEmployeeIdAndAttendanceDateBetweenAndStatus(empId, "ABSENT", thirtyDaysAgo, LocalDate.now());
            int leaveDays = (int) attendanceRepository
                    .countByEmployeeIdAndAttendanceDateBetweenAndStatusIn(empId, List.of("LEAVE", "ON_LEAVE"), thirtyDaysAgo, LocalDate.now());
            int lateArriv = (int) recentAttendance.stream()
                    .filter(att -> Boolean.TRUE.equals(att.getHasCheckedIn()))
                    .filter(att -> isAfterNineThirty(att.getWorkHours()))
                    .count();
            Double avgHours = calculateAverageWorkHours(recentAttendance);

            Optional<Attendance> latestAttendance = attendanceRepository
                    .findFirstByEmployeeIdOrderByAttendanceDateDesc(empId);
            String availStatus = latestAttendance.map(Attendance::getStatus).orElse(null);
            String checkedIn = latestAttendance.map(Attendance::getHasCheckedIn)
                    .map(v -> v ? "Checked In" : "Not Checked In")
                    .orElse(null);

            String phone = contactRepository.findFirstByRefTableIdOrderByIdDesc(empId)
                    .map(c -> c.getContact()).orElse(null);

            String branchName = employee.getBranch() != null ? employee.getBranch().getBranchName() : null;
            String regionName = employee.getBranch() != null && employee.getBranch().getRegion() != null
                    ? employee.getBranch().getRegion().getRegionName() : null;
            String location = branchName != null ? branchName : (employee.getBranch() != null ? "Branch " + employee.getBranch().getId() : null);

            Optional<EmployeeLeaveAccount> leaveAccount = leaveAccountRepository.findFirstByEmployeeIdOrderByIdDesc(empId);
            Double leaveBalance = leaveAccount.map(EmployeeLeaveAccount::getBalance).orElse(0.0);
            Double creditedLeaves = leaveAccount.map(EmployeeLeaveAccount::getCreditedLeaves).orElse(0.0);

            Double perfScore = appraisalEvaluationRepository.findAverageScoreByEmployeeId(empId);
            perfScore = perfScore == null ? 0.0 : perfScore;

            long productivityPresent = attendanceRepository
                    .countByEmployeeIdAndAttendanceDateBetweenAndStatus(empId, "PRESENT", ninetyDaysAgo, LocalDate.now());
            long productivityTotal = attendanceRepository
                    .countByEmployeeIdAndAttendanceDateBetween(empId, ninetyDaysAgo, LocalDate.now());
            Double prodScore = productivityTotal == 0 ? 0.0
                    : Math.round((productivityPresent * 100.0 / productivityTotal) * 100.0) / 100.0;

            String apprRating = appraisalReviewRepository.findFirstByEmployeeIdOrderByIdDesc(empId)
                    .map(AppraisalReview::getAppraisalStatus)
                    .orElse(null);

            Optional<TechvgUser> techUser = techvgUserRepository.findFirstByEmployeeIdOrderByIdDesc(empId);
            String profileImage = techUser.map(TechvgUser::getImageUrl).orElse(null);
            String lastLogin = techUser.map(TechvgUser::getLastModifiedDate).map(Object::toString).orElse(null);
            String accountStatus = techUser.map(TechvgUser::getActivated).map(String::valueOf).orElse(null);
            String accountCreatedAt = techUser.map(TechvgUser::getCreatedDate).map(Object::toString).orElse(null);

            List<Map<String, Object>> perfRows = performanceReviewRepository.findPerformanceTrendsByEmployeeId(empId)
                    .stream()
                    .map(trend -> of("month", trend.getMonth(), "score", trend.getScore()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> educations = educationRepository.findByEmployeeIdOrderByStartYearDesc(empId)
                    .stream()
                    .map(edu -> of(
                            "id", edu.getId(),
                            "educationType", edu.getEducationType(),
                            "institution", edu.getInstitution(),
                            "startYear", edu.getStartYear() != null ? edu.getStartYear().toString() : null,
                            "endDate", edu.getEndDate() != null ? edu.getEndDate().toString() : null,
                            "grade", edu.getGrade(),
                            "description", edu.getDescription()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> experiences = workExperienceRepository.findByEmployeeIdOrderByStartDateDesc(empId)
                    .stream()
                    .map(exp -> of(
                            "id", exp.getId(),
                            "companyName", exp.getCompanyName(),
                            "title", exp.getJobTitle(),
                            "startDate", exp.getStartDate() != null ? exp.getStartDate().toString() : null,
                            "endDate", exp.getEndDate() != null ? exp.getEndDate().toString() : null,
                            "description", exp.getJobDesc()))
                    .collect(Collectors.toList());

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

            List<Map<String, Object>> workPayload = experiences.stream()
                    .map(exp -> of(
                            "title", exp.get("title"),
                            "companyName", exp.get("companyName"),
                            "startDate", exp.get("startDate"),
                            "endDate", exp.get("endDate"),
                            "description", exp.get("description")))
                    .collect(Collectors.toList());

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("employee",         empPayload);
            resp.put("attendance",       of("totalDays", totalDays, "presentDays", presentDays,
                                            "absentDays", absentDays,
                                            "leaveDays", leaveDays, "lateArrivals", lateArriv,
                                            "averageWorkHours", avgHours, "availabilityStatus", availStatus,
                                            "currentActivity", checkedIn,
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

    private boolean isAfterNineThirty(String workHours) {
        if (workHours == null || workHours.isBlank()) return false;
        try {
            String normalized = workHours.trim();
            String[] segments = normalized.split(":");
            if (segments.length < 2) return false;
            int hours = Integer.parseInt(segments[0]);
            int minutes = Integer.parseInt(segments[1]);
            return hours > 9 || (hours == 9 && minutes > 30);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private Double calculateAverageWorkHours(List<Attendance> attendanceList) {
        List<Long> seconds = attendanceList.stream()
                .map(Attendance::getWorkHours)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::timeStringToSeconds)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (seconds.isEmpty()) return null;
        double averageSeconds = seconds.stream().mapToLong(Long::longValue).average().orElse(0.0);
        return Math.round((averageSeconds / 3600.0) * 100.0) / 100.0;
    }

    private Long timeStringToSeconds(String timeString) {
        try {
            String[] splits = timeString.split(":");
            if (splits.length < 2) return null;
            int hours = Integer.parseInt(splits[0]);
            int minutes = Integer.parseInt(splits[1]);
            int seconds = splits.length == 3 ? Integer.parseInt(splits[2]) : 0;
            return (long) hours * 3600 + minutes * 60 + seconds;
        } catch (NumberFormatException ex) {
            return null;
        }
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
