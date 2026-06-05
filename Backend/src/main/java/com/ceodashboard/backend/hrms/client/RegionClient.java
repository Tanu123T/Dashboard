package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.RegionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsRegionClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface RegionClient {

    @GetMapping("/api/regions/{id}")
    RegionDTO getRegionById(@PathVariable("id") Long id);

    @GetMapping("/api/regions")
    List<RegionDTO> getAllRegions(@RequestParam(value = "companyId.equals", required = false) Integer companyId);
}
