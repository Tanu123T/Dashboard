package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.WorkingHoursDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsWorkingHoursClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface WorkingHoursClient {

    @GetMapping("/api/working-hours/{id}")
    WorkingHoursDTO getWorkingHoursById(@PathVariable("id") Long id);

    @GetMapping("/api/working-hours")
    List<WorkingHoursDTO> getAllWorkingHours(@RequestParam("companyId.equals") Integer companyId);

    @GetMapping("/api/working-hours/branch/{branchId}")
    List<WorkingHoursDTO> getWorkingHoursByBranch(@PathVariable("branchId") Long branchId);
}
