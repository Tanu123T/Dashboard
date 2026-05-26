package com.ceodashboard.backend.hrms.controller;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.service.WorkforceHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v3/hrms/workforce-health")
@RequiredArgsConstructor
@Slf4j
public class WorkforceHealthController {

    private final WorkforceHealthService workforceHealthService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('CEO', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<WorkforceHealthSummaryResponse>> getWorkforceHealthSummary() {
        log.info("API call: GET /api/v3/hrms/workforce-health/summary");
        try {
            WorkforceHealthSummaryResponse summary = workforceHealthService.getWorkforceHealthSummary();
            return ResponseEntity.ok(ApiResponse.success(summary, "Workforce health summary retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching workforce health summary", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Error fetching summary", e.getMessage()));
        }
    }

    @GetMapping("/headcount-trend")
    @PreAuthorize("hasAnyRole('CEO', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<WorkforceHealthTrendResponse>>> getHeadcountTrend() {
        log.info("API call: GET /api/v3/hrms/workforce-health/headcount-trend");
        try {
            List<WorkforceHealthTrendResponse> trends = workforceHealthService.getHeadcountTrend();
            return ResponseEntity.ok(ApiResponse.success(trends, "Headcount trend retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching headcount trend", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Error fetching trend", e.getMessage()));
        }
    }

    @GetMapping("/attendance-log")
    @PreAuthorize("hasAnyRole('CEO', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<WorkforceHealthAttendanceLogResponse>>> getAttendanceLog(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {
        log.info("API call: GET /api/v3/hrms/workforce-health/attendance-log");
        try {
            Page<WorkforceHealthAttendanceLogResponse> logs = workforceHealthService.getAttendanceLog(
                    searchTerm, departmentId, fromDate, toDate, pageable);
            return ResponseEntity.ok(ApiResponse.success(logs, "Attendance log retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching attendance log", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Error fetching logs", e.getMessage()));
        }
    }

    @GetMapping("/watchlist")
    @PreAuthorize("hasAnyRole('CEO', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<WorkforceHealthWatchlistResponse>>> getWorkforceHealthWatchlist() {
        log.info("API call: GET /api/v3/hrms/workforce-health/watchlist");
        try {
            List<WorkforceHealthWatchlistResponse> watchlist = workforceHealthService.getWorkforceHealthWatchlist();
            return ResponseEntity
                    .ok(ApiResponse.success(watchlist, "Workforce health watchlist retrieved successfully"));
        } catch (Exception e) {
            log.error("Error fetching workforce health watchlist", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(500, "Error fetching watchlist", e.getMessage()));
        }
    }
}
