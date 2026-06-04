package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.EmployeeLeaveAccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsEmployeeLeaveAccountClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface EmployeeLeaveAccountClient {

    @GetMapping("/api/employee-leave-accounts/{id}")
    EmployeeLeaveAccountDTO getLeaveAccountById(@PathVariable("id") Long id);

    @GetMapping("/api/employee-leave-accounts/employee/{empId}")
    List<EmployeeLeaveAccountDTO> getLeaveAccountsByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/employee-leave-accounts/employee/{empId}/leave-type/{leaveTypeId}")
    EmployeeLeaveAccountDTO getLeaveAccountByEmployeeIdAndLeaveTypeId(
        @PathVariable("empId") String empId,
        @PathVariable("leaveTypeId") Long leaveTypeId
    );
}
