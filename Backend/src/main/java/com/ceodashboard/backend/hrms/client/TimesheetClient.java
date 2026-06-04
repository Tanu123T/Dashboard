package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.TimesheetDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsTimesheetClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface TimesheetClient {

    @GetMapping("/api/time-sheets/{id}")
    TimesheetDTO getTimesheetById(@PathVariable("id") Long id);

    @GetMapping("/api/time-sheets/employee/{empId}")
    List<TimesheetDTO> getTimesheetByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/time-sheets/employee/{empId}/date-range")
    List<TimesheetDTO> getTimesheetByEmployeeIdAndDateRange(
        @PathVariable("empId") String empId,
        @RequestParam("fromDate") String fromDate,
        @RequestParam("toDate") String toDate
    );
}
