package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.ApiResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeDashboardResponse;
import com.ceodashboard.backend.hrms.dto.EmployeeHubItemDTO;
import com.ceodashboard.backend.hrms.dto.EmployeeHubPageDTO;
import com.ceodashboard.backend.hrms.exception.ResourceNotFoundException;
import com.ceodashboard.backend.hrms.repository.EmployeeDashboardRepository;
import com.ceodashboard.backend.hrms.service.EmployeeDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hrms/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeDashboardController {

    private final EmployeeDashboardService     dashboardService;
    private final EmployeeDashboardRepository  dashboardRepository;

    // ── Employee Hub list ────────────────────────────────────────────────────
    /**
     * GET /api/v1/hrms/employees/hub-list?page=0&size=100
     * Returns enriched employee cards for the hub page:
     * full name, designation, department, branch, region, status, profile image.
     */
    @GetMapping("/hub-list")
    public ResponseEntity<ApiResponse<EmployeeHubPageDTO>> getHubList(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "100") int size) {

        log.info("Hub list request: page={}, size={}", page, size);
        int offset = page * size;

        List<EmployeeHubItemDTO> content = dashboardRepository.findHubEmployees(size, offset);
        long total   = dashboardRepository.countHubEmployees();
        int  pages   = (int) Math.ceil((double) total / size);

        EmployeeHubPageDTO result = EmployeeHubPageDTO.builder()
                .content(content)
                .totalElements(total)
                .totalPages(pages)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(ApiResponse.success(result, "Hub list fetched successfully"));
    }

    // ── Full employee dashboard ──────────────────────────────────────────────
    /**
     * GET /api/v1/hrms/employees/{id}/dashboard
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<ApiResponse<EmployeeDashboardResponse>> getEmployeeDashboard(
            @PathVariable Long id) {

        log.info("Dashboard request for employeeId={}", id);
        try {
            EmployeeDashboardResponse dashboard = dashboardService.getDashboard(id);
            return ResponseEntity.ok(ApiResponse.success(dashboard, "Employee dashboard fetched successfully"));
        } catch (ResourceNotFoundException ex) {
            log.warn("Dashboard: employee not found — id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, ex.getMessage(), "Not Found"));
        } catch (Exception ex) {
            log.error("Dashboard error for employeeId={}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500,
                            "Failed to build employee dashboard",
                            ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }
}
