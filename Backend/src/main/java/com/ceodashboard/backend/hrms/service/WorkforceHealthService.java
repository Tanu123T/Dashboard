package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WorkforceHealthService {

    /**
     * Get workforce health summary metrics
     */
    WorkforceHealthSummaryResponse getWorkforceHealthSummary();

    /**
     * Get headcount trend by month
     */
    List<WorkforceHealthTrendResponse> getHeadcountTrend();

    /**
     * Get paginated attendance log with filtering and sorting
     */
    Page<WorkforceHealthAttendanceLogResponse> getAttendanceLog(
            String searchTerm,
            Long departmentId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);

    /**
     * Get workforce health watchlist alerts
     */
    List<WorkforceHealthWatchlistResponse> getWorkforceHealthWatchlist();
}
