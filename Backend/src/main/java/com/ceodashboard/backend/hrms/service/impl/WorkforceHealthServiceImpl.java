package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.entity.Attendance;
import com.ceodashboard.backend.hrms.mapper.WorkforceHealthMapper;
import com.ceodashboard.backend.hrms.repository.WorkforceHealthRepository;
import com.ceodashboard.backend.hrms.service.WorkforceHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkforceHealthServiceImpl implements WorkforceHealthService {

        private final WorkforceHealthRepository repository;
        private final WorkforceHealthMapper mapper;

        @Override
        public WorkforceHealthSummaryResponse getWorkforceHealthSummary() {
                log.info("Generating workforce health summary");

                Long presentToday = repository.countEmployeePresentToday();
                Long onBreak = 0L; // You may need to add this to repository if needed
                Long onLeave = repository.countEmployeesOnLeaveToday();
                Long lateArrivals = repository.countLateArrivals();
                Long presentInOffice = repository.countPresentInOffice();
                Double attendanceConsistency = repository.calculateAttendanceConsistency();

                return WorkforceHealthSummaryResponse.builder()
                                .presentToday(presentToday)
                                .onBreak(onBreak)
                                .onLeave(onLeave)
                                .lateArrivals(lateArrivals)
                                .presentInOffice(presentInOffice)
                                .attendanceConsistency(attendanceConsistency != null ? attendanceConsistency : 0.0)
                                .build();
        }

        @Override
        public List<WorkforceHealthTrendResponse> getHeadcountTrend() {
                log.info("Fetching headcount trend");

                List<Map<String, Object>> results = repository.getHeadcountTrendByMonth();

                return results.stream()
                                .map(mapper::toTrendResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public Page<WorkforceHealthAttendanceLogResponse> getAttendanceLog(
                        String searchTerm,
                        Long departmentId,
                        LocalDate fromDate,
                        LocalDate toDate,
                        Pageable pageable) {
                log.info("Fetching attendance log - department: {}, fromDate: {}, toDate: {}",
                                departmentId, fromDate, toDate);

                // Get filtered attendance records (as Maps to avoid entity loading)
                List<Map<String, Object>> attendanceRecords = repository.getFilteredAttendanceLog(
                                searchTerm, departmentId, fromDate, toDate);

                // Convert Maps to DTOs
                List<WorkforceHealthAttendanceLogResponse> dtoList = attendanceRecords.stream()
                                .map(mapper::toAttendanceLogResponseFromMap)
                                .collect(Collectors.toList());

                // Apply pagination manually
                int pageNumber = pageable.getPageNumber();
                int pageSize = pageable.getPageSize();
                int start = pageNumber * pageSize;
                int end = Math.min((start + pageSize), dtoList.size());

                List<WorkforceHealthAttendanceLogResponse> pageContent = dtoList.subList(start, end);

                return new PageImpl<>(pageContent, pageable, dtoList.size());
        }

        @Override
        public List<WorkforceHealthWatchlistResponse> getWorkforceHealthWatchlist() {
                log.info("Generating workforce health watchlist");

                List<Map<String, Object>> atRiskRecords = repository.getAtRiskEmployees();

                return atRiskRecords.stream()
                                .map(mapper::toWatchlistResponseFromMap)
                                .collect(Collectors.toList());
        }
}
