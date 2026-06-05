package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.HolidayDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsHolidayClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface HolidayClient {

    @GetMapping("/api/holidays/{id}")
    HolidayDTO getHolidayById(@PathVariable("id") Long id);

    @GetMapping("/api/holidays")
    List<HolidayDTO> getAllHolidays(@RequestParam("companyId.equals") Integer companyId);

    @GetMapping("/api/holidays/year/{year}")
    List<HolidayDTO> getHolidaysByYear(
        @PathVariable("year") Integer year,
        @RequestParam("companyId.equals") Integer companyId
    );
}
