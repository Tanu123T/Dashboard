package com.ceodashboard.backend.hrms.service.impl;

import com.ceodashboard.backend.hrms.client.*;
import com.ceodashboard.backend.hrms.dto.*;
import com.ceodashboard.backend.hrms.service.HrmsDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HrmsDataServiceImpl implements HrmsDataService {

    private static final Logger logger = LoggerFactory.getLogger(HrmsDataServiceImpl.class);

    private final BranchClient branchClient;
    private final RegionClient regionClient;
    private final HolidayClient holidayClient;
    private final WorkingHoursClient workingHoursClient;

    @Value("${hrms.company.id:1}")
    private Integer companyId;

    public HrmsDataServiceImpl(
            BranchClient branchClient,
            RegionClient regionClient,
            HolidayClient holidayClient,
            WorkingHoursClient workingHoursClient) {
        this.branchClient = branchClient;
        this.regionClient = regionClient;
        this.holidayClient = holidayClient;
        this.workingHoursClient = workingHoursClient;
    }

    @Override
    @Cacheable(value = "branches")
    public List<BranchDTO> getAllBranches() {
        logger.debug("Fetching all branches for company: {}", companyId);
        try {
            List<BranchDTO> branches = branchClient.getAllBranches(companyId);
            logger.debug("Successfully fetched {} branches", branches.size());
            return branches;
        } catch (Exception e) {
            logger.error("Error fetching branches", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "regions")
    public List<RegionDTO> getAllRegions() {
        logger.debug("Fetching all regions for company: {}", companyId);
        try {
            List<RegionDTO> regions = regionClient.getAllRegions(companyId);
            logger.debug("Successfully fetched {} regions", regions.size());
            return regions;
        } catch (Exception e) {
            logger.error("Error fetching regions", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "holidays")
    public List<HolidayDTO> getAllHolidays() {
        logger.debug("Fetching all holidays for company: {}", companyId);
        try {
            List<HolidayDTO> holidays = holidayClient.getAllHolidays(companyId);
            logger.debug("Successfully fetched {} holidays", holidays.size());
            return holidays;
        } catch (Exception e) {
            logger.error("Error fetching holidays", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "working-hours")
    public List<WorkingHoursDTO> getAllWorkingHours() {
        logger.debug("Fetching all working hours for company: {}", companyId);
        try {
            List<WorkingHoursDTO> workingHours = workingHoursClient.getAllWorkingHours(companyId);
            logger.debug("Successfully fetched {} working hours records", workingHours.size());
            return workingHours;
        } catch (Exception e) {
            logger.error("Error fetching working hours", e);
            return List.of();
        }
    }

    @Override
    public List<HolidayDTO> getHolidaysByYear(Integer year) {
        logger.debug("Fetching holidays for year: {} and company: {}", year, companyId);
        try {
            List<HolidayDTO> holidays = holidayClient.getHolidaysByYear(year, companyId);
            logger.debug("Successfully fetched {} holidays for year {}", holidays.size(), year);
            return holidays;
        } catch (Exception e) {
            logger.error("Error fetching holidays for year: {}", year, e);
            return List.of();
        }
    }

    @Override
    public List<WorkingHoursDTO> getWorkingHoursByBranch(Long branchId) {
        logger.debug("Fetching working hours for branch: {}", branchId);
        try {
            List<WorkingHoursDTO> workingHours = workingHoursClient.getWorkingHoursByBranch(branchId);
            logger.debug("Successfully fetched {} working hours for branch {}", workingHours.size(), branchId);
            return workingHours;
        } catch (Exception e) {
            logger.error("Error fetching working hours for branch: {}", branchId, e);
            return List.of();
        }
    }

    @Override
    public List<BranchDTO> getBranchesByRegion(Long regionId) {
        logger.debug("Fetching branches for region: {}", regionId);
        try {
            List<BranchDTO> branches = branchClient.getBranchesByRegion(regionId);
            logger.debug("Successfully fetched {} branches for region {}", branches.size(), regionId);
            return branches;
        } catch (Exception e) {
            logger.error("Error fetching branches for region: {}", regionId, e);
            return List.of();
        }
    }
}
