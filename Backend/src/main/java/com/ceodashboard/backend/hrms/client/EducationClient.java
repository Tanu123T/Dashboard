package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.EducationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsEducationClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface EducationClient {

    @GetMapping("/api/educations/{id}")
    EducationDTO getEducationById(@PathVariable("id") Long id);

    @GetMapping("/api/educations/employee/{empId}")
    List<EducationDTO> getEducationByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/educations")
    List<EducationDTO> getAllEducations(@RequestParam(value = "companyId", required = false) Integer companyId);
}
