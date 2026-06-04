package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.LeaveApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsLeaveApplicationClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface LeaveApplicationClient {

    @GetMapping("/api/leave-applications/{id}")
    LeaveApplicationDTO getLeaveApplicationById(@PathVariable("id") Long id);

    @GetMapping("/api/leave-applications/employee/{empId}")
    List<LeaveApplicationDTO> getLeaveApplicationsByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/leave-applications/employee/{empId}/status/{status}")
    List<LeaveApplicationDTO> getLeaveApplicationsByEmployeeIdAndStatus(
        @PathVariable("empId") String empId,
        @PathVariable("status") String status
    );

    @PostMapping("/api/leave-applications")
    LeaveApplicationDTO createLeaveApplication(@RequestBody LeaveApplicationDTO leaveApplicationDTO);

    @PutMapping("/api/leave-applications/{id}")
    LeaveApplicationDTO updateLeaveApplication(
        @PathVariable("id") Long id,
        @RequestBody LeaveApplicationDTO leaveApplicationDTO
    );

    @GetMapping("/api/leave-applications")
    List<LeaveApplicationDTO> getAllLeaveApplications(@RequestParam(value = "companyId.equals", required = false) Integer companyId);
}
