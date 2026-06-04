package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsEmployeeClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface EmployeeClient {

    @GetMapping("/api/employees/{empId}")
    EmployeeDTO getEmployeeById(@PathVariable("empId") String empId);

    @GetMapping("/api/employees")
    List<EmployeeDTO> getAllEmployees(@RequestParam(value = "companyId", required = false) Integer companyId);

    @GetMapping("/api/employees/search")
    List<EmployeeDTO> searchEmployees(
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "companyId", required = false) Integer companyId
    );

    @GetMapping("/api/employees/{empId}/profile")
    EmployeeDTO getEmployeeProfile(@PathVariable("empId") String empId);

    @GetMapping("/api/employees")
    List<EmployeeDTO> getEmployeesByCompany(@RequestParam("companyId") Integer companyId);

    @GetMapping("/api/employees/{empId}/reporting-manager")
    EmployeeDTO getReportingManager(@PathVariable("empId") String empId);
}
