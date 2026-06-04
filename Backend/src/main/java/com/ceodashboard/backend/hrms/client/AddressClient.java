package com.ceodashboard.backend.hrms.client;

import com.ceodashboard.backend.hrms.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "hrmsAddressClient",
    url = "${hrms.api.base-url}",
    configuration = {
        com.ceodashboard.backend.hrms.config.FeignClientConfiguration.class,
        com.ceodashboard.backend.hrms.config.FeignRequestInterceptor.class,
        com.ceodashboard.backend.hrms.config.FeignErrorDecoder.class
    }
)
public interface AddressClient {

    @GetMapping("/api/addresses/{id}")
    AddressDTO getAddressById(@PathVariable("id") Long id);

    @GetMapping("/api/addresses/employee/{empId}")
    List<AddressDTO> getAddressesByEmployeeId(@PathVariable("empId") String empId);

    @GetMapping("/api/addresses")
    List<AddressDTO> getAllAddresses(@RequestParam(value = "companyId", required = false) Integer companyId);
}
