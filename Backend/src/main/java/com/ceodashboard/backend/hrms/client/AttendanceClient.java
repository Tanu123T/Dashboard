package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.AttendanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient(
    name = "hrmsAttendanceClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface AttendanceClient {

    @GetMapping("/api/attendances/{id}")
    AttendanceDTO getAttendanceById(@PathVariable("id") Long id);

    @GetMapping("/api/attendances/employee/{empId}")
    List<AttendanceDTO> getAttendanceByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/attendances/employee/{empId}/date-range")
    List<AttendanceDTO> getAttendanceByEmployeeIdAndDateRange(
        @PathVariable("empId") String empId,
        @RequestParam("fromDate") String fromDate,
        @RequestParam("toDate") String toDate
    );

    @GetMapping("/api/attendances")
    List<AttendanceDTO> getAttendanceByCompanyId(@RequestParam("companyId") Integer companyId);

    @GetMapping("/api/attendances/employee/{empId}/status/{status}")
    List<AttendanceDTO> getAttendanceByEmployeeIdAndStatus(
        @PathVariable("empId") String empId,
        @PathVariable("status") String status
    );
}
