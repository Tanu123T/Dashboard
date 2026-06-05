package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.*;
import java.time.LocalDate;
import com.ceodashboard.backend.hrms.service.HrmsProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hrms/employees")
@ConditionalOnProperty(prefix = "hrms.db", name = "enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class HrmsApiProxyController {

    private final HrmsProxyService hrmsProxyService;

    @Value("${hrms.company.id:1}")
    private Integer companyId;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EmployeeProfileDTO>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be > 0");
        }

        log.info("Proxy employee list request page={} size={} companyId={}", page, size, companyId);
        List<EmployeeDTO> employees = hrmsProxyService.getAllEmployees(companyId);
        List<EmployeeProfileDTO> profileList = employees.stream()
                .map(this::toProfileDTO)
                .collect(Collectors.toList());

        int total = profileList.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<EmployeeProfileDTO> pageContent = profileList.subList(start, end);

        Page<EmployeeProfileDTO> result = new PageImpl<>(pageContent, PageRequest.of(page, size), total);
        return ResponseEntity.ok(ApiResponse.success(result, "Employees fetched successfully"));
    }

    @GetMapping("/hub-list")
    public ResponseEntity<ApiResponse<EmployeeHubPageDTO>> getEmployeeHubList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and size must be > 0");
        }

        log.info("Proxy hub list request page={} size={} companyId={}", page, size, companyId);
        List<EmployeeDTO> employees = hrmsProxyService.getAllEmployees(companyId);
        List<EmployeeHubItemDTO> hubItems = employees.stream()
                .map(this::toHubItemDTO)
                .collect(Collectors.toList());

        int total = hubItems.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<EmployeeHubItemDTO> pageContent = hubItems.subList(start, end);

        EmployeeHubPageDTO result = EmployeeHubPageDTO.builder()
                .content(pageContent)
                .totalElements(total)
                .totalPages((total + size - 1) / size)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(ApiResponse.success(result, "Employee hub list fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getEmployeeById(@PathVariable String id) {
        log.info("Proxy employee by id request id={}", id);
        EmployeeDTO employee = hrmsProxyService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(toProfileDTO(employee), "Employee fetched successfully"));
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard(
            @PathVariable String id) {
        log.info("Proxy employee dashboard request for employeeId={}", id);
        try {
            EmployeeDTO employee = hrmsProxyService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error(404, "Employee not found", "No employee with id: " + id));
            }

            // 1. Profile Header
            ProfileHeaderDTO profileHeader = ProfileHeaderDTO.builder()
                    .employeeId(employee.getId() != null ? employee.getId() : parseId(employee.getEmpUniqueId()))
                    .fullName(employee.getFullName() != null ? employee.getFullName() : buildFullName(employee))
                    .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                    .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                    .profileImage(null)
                    .officialEmail(employee.getEmailId())
                    .build();

            // 2. Personal Info
            LocalDate joinDate = employee.getJoinDate() != null ? employee.getJoinDate() : employee.getEmploymentDate();
            int expYears = employee.getExperienceYears() != null ? employee.getExperienceYears().intValue() : 0;
            PersonalInfoDTO personalInfo = PersonalInfoDTO.builder()
                    .officialEmail(employee.getEmailId())
                    .phoneNumber(employee.getMobileNo())
                    .branch(null)
                    .region(null)
                    .joinDate(joinDate)
                    .experienceYears(expYears)
                    .experienceMonths(0)
                    .totalExperience(expYears + "y 0m")
                    .reportingManagerName(employee.getReportingManagerName())
                    .build();

            // 3. Attendance Analytics (computed over last 30 days)
            List<AttendanceDTO> attendanceRecords = hrmsProxyService.getAttendanceByEmployeeId(id);
            LocalDate now = LocalDate.now();
            LocalDate windowStart = now.minusDays(30);

            long presentDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    !a.getDate().isBefore(windowStart) &&
                    !a.getDate().isAfter(now) &&
                    ("PRESENT".equalsIgnoreCase(a.getStatus()) || Boolean.TRUE.equals(a.getHasCheckedIn())))
                .count();

            long absentDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    !a.getDate().isBefore(windowStart) &&
                    !a.getDate().isAfter(now) &&
                    "ABSENT".equalsIgnoreCase(a.getStatus()))
                .count();

            long totalDays = attendanceRecords.stream()
                .filter(a -> a.getDate() != null &&
                    !a.getDate().isBefore(windowStart) &&
                    !a.getDate().isAfter(now))
                .count();

            double attendancePercentage = 0.0;
            if (totalDays > 0) {
                attendancePercentage = Math.round((presentDays * 100.0 / totalDays) * 100.0) / 100.0;
            }

            AttendanceAnalyticsDTO attendanceAnalytics = AttendanceAnalyticsDTO.builder()
                    .totalDays((int) totalDays)
                    .presentDays((int) presentDays)
                    .absentDays((int) absentDays)
                    .attendancePercentage(attendancePercentage)
                    .build();

            // 4. Education
            List<EducationDTO> educationList = hrmsProxyService.getEducationByEmployeeId(id);

            EmployeeDashboardResponse result = EmployeeDashboardResponse.builder()
                    .profileHeader(profileHeader)
                    .personalInfo(personalInfo)
                    .attendanceAnalytics(attendanceAnalytics)
                    .performanceTrends(Collections.emptyList())
                    .skills(Collections.emptyList())
                    .projects(Collections.emptyList())
                    .education(educationList)
                    .achievements(Collections.emptyList())
                    .certifications(Collections.emptyList())
                    .workExperience(Collections.emptyList())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(result, "Employee dashboard fetched successfully"));

        } catch (Exception ex) {
            log.error("Dashboard error for employeeId={}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Failed to build employee dashboard",
                            ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    private EmployeeProfileDTO toProfileDTO(EmployeeDTO employee) {
        Long id = employee.getId() != null ? employee.getId() : parseId(employee.getEmpUniqueId());
        return EmployeeProfileDTO.builder()
                .id(id)
                .employeeCode(employee.getEmpUniqueId())
                .fullName(employee.getFullName() != null ? employee.getFullName() : buildFullName(employee))
                .officialEmail(employee.getEmailId())
                .profileImage(null)
                .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .workMode(null)
                .employmentType(null)
                .employeeStatus(employee.getStatus())
                .reportingManagerName(employee.getReportingManagerName())
                .build();
    }

    private EmployeeHubItemDTO toHubItemDTO(EmployeeDTO employee) {
        Long id = employee.getId() != null ? employee.getId() : parseId(employee.getEmpUniqueId());
        return EmployeeHubItemDTO.builder()
                .id(id)
                .employeeCode(employee.getEmpUniqueId())
                .fullName(employee.getFullName() != null ? employee.getFullName() : buildFullName(employee))
                .designation(employee.getDesignation() != null ? employee.getDesignation().getName() : null)
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .branchName(null)
                .regionName(null)
                .status(employee.getStatus())
                .profileImage(null)
                .experienceYears(employee.getExperienceYears() != null ? employee.getExperienceYears().intValue() : 0)
                .build();
    }

    private Long parseId(String empUniqueId) {
        if (empUniqueId == null) {
            return null;
        }
        try {
            return Long.parseLong(empUniqueId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildFullName(EmployeeDTO employee) {
        String firstName = employee.getFirstName() != null ? employee.getFirstName().trim() : "";
        String lastName = employee.getLastName() != null ? employee.getLastName().trim() : "";
        return (firstName + " " + lastName).trim();
    }
}
