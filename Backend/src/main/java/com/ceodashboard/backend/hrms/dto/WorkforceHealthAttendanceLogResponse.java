package com.ceodashboard.backend.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Workforce Health Attendance Log Response DTO
 * 
 * Contains attendance information for a single employee on a specific date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkforceHealthAttendanceLogResponse {

    private Long attendanceId;

    /** Employee ID */
    private Long employeeId;

    /** Employee Department */
    private String department;

    /** Attendance date */
    private LocalDate attendanceDate;

    /** Whether employee checked in */
    private Boolean hasCheckedIn;

    /** Attendance status: Present, Absent, or Late */
    private String status;

    /** Total working hours */
    private String workHours;

    /** Checkout time */
    private String checkOut;
}
