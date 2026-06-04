package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.BranchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsBranchClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface BranchClient {

    @GetMapping("/api/branches/{id}")
    BranchDTO getBranchById(@PathVariable("id") Long id);

    @GetMapping("/api/branches")
    List<BranchDTO> getAllBranches(@RequestParam(value = "companyId", required = false) Integer companyId);

    @GetMapping("/api/branches/region/{regionId}")
    List<BranchDTO> getBranchesByRegion(@PathVariable("regionId") Long regionId);
}
