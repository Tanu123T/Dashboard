package com.ceodashboard.backend.hrms.service;

import com.ceodashboard.backend.hrms.dto.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface HrmsDataService {

    @Cacheable(value = "branches")
    List<BranchDTO> getAllBranches();

    @Cacheable(value = "regions")
    List<RegionDTO> getAllRegions();

    @Cacheable(value = "holidays")
    List<HolidayDTO> getAllHolidays();

    @Cacheable(value = "working-hours")
    List<WorkingHoursDTO> getAllWorkingHours();

    List<HolidayDTO> getHolidaysByYear(Integer year);

    List<WorkingHoursDTO> getWorkingHoursByBranch(Long branchId);

    List<BranchDTO> getBranchesByRegion(Long regionId);
}
