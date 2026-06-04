package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.client.AttendanceClient;
import com.ceodashboard.backend.hrms.client.EmployeeClient;
import com.ceodashboard.backend.hrms.dto.AttendanceDTO;
import com.ceodashboard.backend.hrms.dto.EmployeeDTO;
import com.ceodashboard.backend.hrms.dto.WorkforceHealthAttendanceLogResponse;
import com.ceodashboard.backend.hrms.dto.WorkforceHealthSummaryResponse;
import com.ceodashboard.backend.hrms.dto.WorkforceHealthTrendResponse;
import com.ceodashboard.backend.hrms.dto.WorkforceHealthWatchlistResponse;
import com.ceodashboard.backend.hrms.mapper.WorkforceHealthMapper;
import com.ceodashboard.backend.hrms.service.WorkforceHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WorkforceHealthServiceImpl implements WorkforceHealthService {

    private final AttendanceClient attendanceClient;
    private final EmployeeClient employeeClient;
    private final WorkforceHealthMapper mapper;

    @Value("${hrms.company.id:1}")
    private Integer companyId;

    @Override
    public WorkforceHealthSummaryResponse getWorkforceHealthSummary() {
        log.info("Generating workforce health summary from HRMS APIs");

        try {
            // Fetch all attendance records for today from HRMS API
            List<AttendanceDTO> attendanceToday = attendanceClient.getAttendanceByCompanyId(companyId)
                .stream()
                .filter(a -> a.getDate() != null && a.getDate().equals(LocalDate.now()))
                .collect(Collectors.toList());

            long presentToday = attendanceToday.stream()
                .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                .count();

            long onLeave = attendanceToday.stream()
                .filter(a -> "LEAVE".equalsIgnoreCase(a.getStatus()))
                .count();

            long lateArrivals = attendanceToday.stream()
                .filter(a -> "LATE".equalsIgnoreCase(a.getStatus()))
                .count();

            long presentInOffice = presentToday;

            double attendanceConsistency = calculateAttendanceConsistency();

            return WorkforceHealthSummaryResponse.builder()
                .presentToday(presentToday)
                .onBreak(0L)
                .onLeave(onLeave)
                .lateArrivals(lateArrivals)
                .presentInOffice(presentInOffice)
                .attendanceConsistency(attendanceConsistency)
                .build();
        } catch (Exception e) {
            log.error("Error generating workforce health summary", e);
            return WorkforceHealthSummaryResponse.builder()
                .presentToday(0L)
                .onBreak(0L)
                .onLeave(0L)
                .lateArrivals(0L)
                .presentInOffice(0L)
                .attendanceConsistency(0.0)
                .build();
        }
    }

    @Override
    public List<WorkforceHealthTrendResponse> getHeadcountTrend() {
        log.info("Fetching headcount trend from HRMS API");

        try {
            // This would require a dedicated API endpoint in HRMS
            // For now, returning empty list
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching headcount trend", e);
            return List.of();
        }
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

        try {
            // Fetch attendance records within date range
            String fromDateStr = fromDate != null ? fromDate.toString() : LocalDate.now().minusMonths(1).toString();
            String toDateStr = toDate != null ? toDate.toString() : LocalDate.now().toString();

            List<AttendanceDTO> attendanceList = attendanceClient
                .getAttendanceByCompanyId(companyId)
                .stream()
                .filter(a -> a.getDate() != null &&
                    a.getDate().isAfter(LocalDate.parse(fromDateStr)) &&
                    a.getDate().isBefore(LocalDate.parse(toDateStr)))
                .collect(Collectors.toList());

            // Convert DTOs to response objects directly
            List<WorkforceHealthAttendanceLogResponse> dtoList = attendanceList.stream()
                .map(att -> WorkforceHealthAttendanceLogResponse.builder()
                    .attendanceId(att.getId())
                    .employeeId(parseEmpId(att.getEmpId()))
                    .attendanceDate(att.getDate())
                    .hasCheckedIn(att.getHasCheckedIn())
                    .status(att.getStatus())
                    .workHours(att.getHours() != null ? String.valueOf(att.getHours()) : null)
                    .checkOut(att.getCheckOutTime())
                    .build())
                .collect(Collectors.toList());

            // Apply pagination manually
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int start = pageNumber * pageSize;
            int end = Math.min((start + pageSize), dtoList.size());

            List<WorkforceHealthAttendanceLogResponse> pageContent = dtoList.subList(start, Math.min(end, dtoList.size()));

            return new PageImpl<>(pageContent, pageable, dtoList.size());
        } catch (Exception e) {
            log.error("Error fetching attendance log", e);
            return new PageImpl<>(List.of(), pageable, 0);
        }
    }

    @Override
    public List<WorkforceHealthWatchlistResponse> getWorkforceHealthWatchlist() {
        log.info("Generating workforce health watchlist from HRMS API");

        try {
            // Fetch all employees and identify at-risk ones based on attendance patterns
            List<EmployeeDTO> employees = employeeClient.getEmployeesByCompany(companyId);

            return employees.stream()
                .map(emp -> {
                    try {
                        String empId = emp.getId() != null ? emp.getId().toString() : emp.getEmpUniqueId();
                        List<AttendanceDTO> attendance = attendanceClient.getAttendanceByEmployeeId(empId);
                        // Simple at-risk calculation: less than 70% attendance
                        long presentDays = attendance.stream()
                            .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                            .count();
                        double attendanceRate = attendance.isEmpty() ? 0 : (double) presentDays / attendance.size();

                        if (attendanceRate < 0.7) {
                            return WorkforceHealthWatchlistResponse.builder()
                                .employeeId(emp.getId() != null ? emp.getId() : parseEmpId(emp.getEmpUniqueId()))
                                .status("AT_RISK")
                                .attendanceDate(null)
                                .riskLevel("HIGH")
                                .build();
                        }
                        return null;
                    } catch (Exception e) {
                        log.warn("Error processing employee: {}", emp.getEmpUniqueId());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating workforce health watchlist", e);
            return List.of();
        }
    }

    private double calculateAttendanceConsistency() {
        try {
            // Get all employees and their recent attendance
            List<EmployeeDTO> employees = employeeClient.getEmployeesByCompany(companyId);
            if (employees.isEmpty()) {
                return 0.0;
            }

            double totalConsistency = 0.0;
            for (EmployeeDTO employee : employees) {
                try {
                    String empId = employee.getId() != null ? employee.getId().toString() : employee.getEmpUniqueId();
                    List<AttendanceDTO> recentAttendance = attendanceClient
                        .getAttendanceByEmployeeId(empId)
                        .stream()
                        .filter(a -> a.getDate() != null &&
                            a.getDate().isAfter(LocalDate.now().minusDays(30)))
                        .collect(Collectors.toList());

                    if (!recentAttendance.isEmpty()) {
                        long presentDays = recentAttendance.stream()
                            .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                            .count();
                        totalConsistency += (double) presentDays / recentAttendance.size();
                    }
                } catch (Exception e) {
                    log.warn("Error calculating consistency for employee: {}", employee.getEmpUniqueId());
                }
            }

            return employees.isEmpty() ? 0.0 : (totalConsistency / employees.size()) * 100;
        } catch (Exception e) {
            log.error("Error calculating attendance consistency", e);
            return 0.0;
        }
    }

    private Long parseEmpId(String empId) {
        if (empId == null) return null;
        try {
            return Long.parseLong(empId);
        } catch (NumberFormatException e) {
            log.debug("Unable to parse empId '{}' to Long", empId);
            return null;
        }
    }
}
